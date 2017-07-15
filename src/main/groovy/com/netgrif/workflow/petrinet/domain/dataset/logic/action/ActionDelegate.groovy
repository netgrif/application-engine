package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.petrinet.domain.Transition
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.FieldWithDefault
import com.netgrif.workflow.petrinet.domain.dataset.FileField
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.workflow.workflow.domain.Case

class ActionDelegate {

    private static final String UNCHANGED_VALUE = "unchangedooo"
    private static final String ALWAYS_GENERATE = "always"
    private static final String ONCE_GENERATE = "once"

    private Case useCase
    ChangedField changedField

    ActionDelegate(Case useCase) {
        this.useCase = useCase
        this.changedField = new ChangedField()
    }

    def copyBehavior(Field field, Transition transition) {
        if (!useCase.hasFieldBehavior(field.objectId, transition.stringId)) {
            useCase.dataSet.get(field.objectId).addBehavior(transition.stringId, transition.dataSet.get(field.objectId).behavior)
        }
    }

    def visible = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.objectId).makeVisible(trans.stringId)
    }

    def editable = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.objectId).makeEditable(trans.stringId)
    }

    def required = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.objectId).makeRequired(trans.stringId)
    }

    def optional = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.objectId).makeOptional(trans.stringId)
    }

    def hidden = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.objectId).makeHidden(trans.stringId)
    }

    def unchanged = { return UNCHANGED_VALUE }

    def make(Field field, Closure behavior) {
        [on: { Transition trans ->
            [when: { Closure condition ->
                if (condition()) {
                    behavior(field, trans)
                    changedField.id = field.objectId
                    changedField.behavior = useCase.dataSet.get(field.objectId).behavior
                }
            }]
        }]
    }

    def saveChangedValue(Field field){
        useCase.dataSet.get(field.objectId).value = field.value
        changedField.id = field.objectId
        changedField.value = field.value
    }

    def change(Field field) {
        [about: { cl ->
            def value = cl()
            if(value instanceof Closure && value() == UNCHANGED_VALUE){
                return
            }
            if(value == null){
                if(field instanceof FieldWithDefault && field.defaultValue != useCase.dataSet.get(field.objectId).value){
                    field.clearValue()
                    saveChangedValue(field)
                } else if(!(field instanceof FieldWithDefault) && useCase.dataSet.get(field.objectId).value != null){
                    field.clearValue()
                    saveChangedValue(field)
                }
                return
            }
            if (value != null) {
                field.value = value
                saveChangedValue(field)
            }
        }]
    }

    def always = { return ALWAYS_GENERATE }
    def once = { return ONCE_GENERATE }

    def generate(String methods, Closure repeated){
        [into: { Field field ->
            File f = new FileGenerateReflection(useCase, field as FileField,repeated() == ALWAYS_GENERATE).callMethod(methods) as File
            /*if(f != null) {
                useCase.dataSet.get(field.objectId).value = f.name
                field.value = f.name
            }*/
        }]
    }
}
