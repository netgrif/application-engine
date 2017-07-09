package com.netgrif.workflow.petrinet.domain.dataset.logic

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

class ChangedField {

    String id
    Map<String, Set<FieldBehavior>> behavior
    Object value

    ChangedField(String id) {
        this.id = id
        behavior = new HashMap<>()
    }

    void merge(ChangedField changedField) {
        if (changedField.value != null)
            this.value = changedField.value
        if (changedField.behavior != null && !changedField.behavior.isEmpty()) {
            changedField.behavior.each { trans, behav ->
                behavior.put(trans, new HashSet<FieldBehavior>(behav))
            }
        }
    }

    ObjectNode toJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode()
        if (!behavior.isEmpty()) {
            ObjectNode b = JsonNodeFactory.instance.objectNode()
            behavior.each { trans, behav -> b.set(trans, behaviorToJson(trans)) }
            node.set("behavior", b)
        }
        if (this.value != null)
            node.put("value", value)

        return node
    }

    private ObjectNode behaviorToJson(String trans) {
        ObjectNode node = JsonNodeFactory.instance.objectNode()
        behavior.get(trans).each { behav -> node.put(behav.toString(), true) }
        return node
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ChangedField that = (ChangedField) o
        if (id != that.id) return false

        return true
    }

    int hashCode() {
        return id.hashCode()
    }
}
