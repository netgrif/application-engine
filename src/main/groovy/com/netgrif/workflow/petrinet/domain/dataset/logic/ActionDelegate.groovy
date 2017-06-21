package com.netgrif.workflow.petrinet.domain.dataset.logic

import com.netgrif.workflow.petrinet.domain.Transition
import com.netgrif.workflow.petrinet.domain.dataset.Field

class ActionDelegate {

    ActionDelegate() {
    }

    def visible = { Field field, Transition trans ->
        trans.dataSet.get(field.objectId).makeVisible()
    }

    def editable = { Field field, Transition trans ->
        trans.dataSet.get(field.objectId).makeEditable()
    }

    def required = { Field field, Transition trans ->
        trans.dataSet.get(field.objectId).makeRequired()
    }

    def optional = { Field field, Transition trans ->
        trans.dataSet.get(field.objectId).makeOptional()
    }

    def make(Field field, Closure behavior) {
        [on: { Transition trans ->
            [when: { Closure condition ->
                if (condition()) behavior(field, trans)
            }]
        }]
    }

    def change(Field field) {
        [about: { cl ->
            def value = cl()
            if (value != null) field.value = value
        }]
    }
}
