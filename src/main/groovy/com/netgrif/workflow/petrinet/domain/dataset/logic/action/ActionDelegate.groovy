package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.petrinet.domain.Transition
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.workflow.workflow.domain.Case

class ActionDelegate {

    private Case useCase
    ChangedField changedField

    ActionDelegate(Case useCase) {
        this.useCase = useCase
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

    def make(Field field, Closure behavior) {
        [on: { Transition trans ->
            [when: { Closure condition ->
                if (condition()) {
                    behavior(field, trans)
                    changedField = new ChangedField(field.objectId)
                    changedField.behavior = useCase.dataSet.get(field.objectId).behavior
                }
            }]
        }]
    }

    def change(Field field) {
        [about: { cl ->
            def value = cl()
            if (value != null) {
                field.value = value
                useCase.dataSet.get(field.objectId).value = value
                changedField = new ChangedField(field.objectId)
                changedField.value = field.value
            }
        }]
    }
}
