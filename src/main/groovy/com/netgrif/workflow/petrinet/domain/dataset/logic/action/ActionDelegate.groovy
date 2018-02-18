package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.petrinet.domain.Transition
import com.netgrif.workflow.petrinet.domain.dataset.*
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.workflow.workflow.domain.Case

@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class ActionDelegate {

    private static final String UNCHANGED_VALUE = "unchangedooo"
    private static final String ALWAYS_GENERATE = "always"
    private static final String ONCE_GENERATE = "once"

    private Case useCase
    private FieldActionsRunner actionsRunner
    ChangedField changedField

    ActionDelegate(Case useCase, FieldActionsRunner actionsRunner) {
        this.useCase = useCase
        this.actionsRunner = actionsRunner
        this.changedField = new ChangedField()
    }

    def copyBehavior(Field field, Transition transition) {
        if (!useCase.hasFieldBehavior(field.stringId, transition.stringId)) {
            useCase.dataSet.get(field.stringId).addBehavior(transition.stringId, transition.dataSet.get(field.stringId).behavior)
        }
    }

    def visible = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeVisible(trans.stringId)
    }

    def editable = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeEditable(trans.stringId)
    }

    def required = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeRequired(trans.stringId)
    }

    def optional = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeOptional(trans.stringId)
    }

    def hidden = { Field field, Transition trans ->
        copyBehavior(field, trans)
        useCase.dataSet.get(field.stringId).makeHidden(trans.stringId)
    }

    def unchanged = { return UNCHANGED_VALUE }

    def make(Field field, Closure behavior) {
        [on: { Transition trans ->
            [when: { Closure condition ->
                if (condition()) {
                    behavior(field, trans)
                    changedField.id = field.stringId
                    changedField.addBehavior(useCase.dataSet.get(field.stringId).behavior)
                }
            }]
        }]
    }

    def saveChangedValue(Field field) {
        useCase.dataSet.get(field.stringId).value = field.value
        changedField.id = field.stringId
        changedField.addAttribute("value", field.value)
    }

    def change(Field field) {
        [about  : { cl ->
            def value = cl()
            if (value instanceof Closure && value() == UNCHANGED_VALUE) {
                return
            }
            if (value == null) {
                if (field instanceof FieldWithDefault && field.defaultValue != useCase.dataSet.get(field.stringId).value) {
                    field.clearValue()
                    saveChangedValue(field)
                } else if (!(field instanceof FieldWithDefault) && useCase.dataSet.get(field.stringId).value != null) {
                    field.clearValue()
                    saveChangedValue(field)
                }
                return
            }
            if (value != null) {
                field.value = value
                saveChangedValue(field)
            }
        },
         choices: { cl ->
             if (!(field instanceof MultichoiceField || field instanceof EnumerationField)) return

             def values = cl()
             if (values == null || (values instanceof Closure && values() == UNCHANGED_VALUE)) return
             if (!(values instanceof Collection)) values = [values]
             field = (ChoiceField) field
             field.choices = values as Set<String>
             changedField.id = field.stringId
             changedField.addAttribute("choices", field.choices)
         }]
    }

    def always = { return ALWAYS_GENERATE }
    def once = { return ONCE_GENERATE }

    def generate(String methods, Closure repeated) {
        [into: { Field field ->
            if (field.type == FieldType.FILE)
                File f = new FileGenerateReflection(useCase, field as FileField, repeated() == ALWAYS_GENERATE).callMethod(methods) as File
            else if (field.type == FieldType.TEXT)
                new TextGenerateReflection(useCase, field as TextField, repeated() == ALWAYS_GENERATE).callMethod(methods) as String
            /*if(f != null) {
                useCase.dataSet.get(field.objectId).value = f.name
                field.value = f.name
            }*/
        }]
    }

    def changeCaseProperty(String property) {
        [about: { cl ->
            def value = cl()
            if (value instanceof Closure && value() == UNCHANGED_VALUE) return
            useCase."$property" = value
        }]
    }

    //Cache manipulation
    def cache(String name, Object value) {
        actionsRunner.addToCache("${useCase.stringId}-${name}", value)
    }

    def cache(String name) {
        return actionsRunner.getFromCache("${useCase.stringId}-${name}" as String)
    }

    def cacheFree(String name) {
        actionsRunner.removeFromCache("${useCase.stringId}-${name}")
    }

    //Get PSC - DSL only for Insurance
    def byCode = { String code ->
        return actionsRunner.postalCodeService.findAllByCode(code)
    }

    def byCity = { String city ->
        return actionsRunner.postalCodeService.findAllByCity(city)
    }

    def psc(Closure find, String input) {
        if (find)
            return find(input)
        return null
    }

    def byIco = { String ico ->
        return actionsRunner.orsrService.findByIco(ico)
    }

    def orsr(Closure find, String ico) {
        return find?.call(ico)
    }
}