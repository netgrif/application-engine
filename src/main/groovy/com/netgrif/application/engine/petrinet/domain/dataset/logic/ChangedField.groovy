package com.netgrif.application.engine.petrinet.domain.dataset.logic

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.workflow.domain.Task
import com.querydsl.core.annotations.QueryExclude

@QueryExclude
class ChangedField {

    String id
    List<TaskPair> changedOn
    Map<String, Object> attributes

    ChangedField() {
        attributes = new HashMap<>()
        changedOn = new ArrayList<>()
    }

    ChangedField(String id) {
        this()
        this.id = id
    }

    void wasChangedOn(Task task) {
        wasChangedOn(task.stringId, task.transitionId)
    }

    void wasChangedOn(String taskId, String transition) {
        if (!isChangedOn(taskId)) {
            changedOn.add(new TaskPair(taskId, transition))
        }
    }

    boolean isChangedOn(String taskId) {
        return changedOn.any { it.taskId == taskId }
    }

    void addAttribute(String name, Object value) {
        if (value instanceof I18nString)
            attributes.put(name, value.defaultValue)
        else
            attributes.put(name, value)
    }

    void addBehavior(Map<String, Set<FieldBehavior>> behavior) {
        Map<String, Map<String, Boolean>> behs = new HashMap<>()
        behavior.each { trans, fieldBehaviors ->
            Map<String, Boolean> b = new HashMap<>()
            fieldBehaviors.each { b.put(it.toString(), true) }
            behs.put(trans, b)
        }
        attributes.put("behavior", behs)
    }

    void merge(ChangedField changedField) {
        changedField.changedOn.each {
            wasChangedOn(it.taskId, it.transition)
        }
        this.attributes.putAll(changedField.attributes)
    }

    Map<String, Object> getAttributes() {
        return attributes
    }

//    ObjectNode toJson() {
//        ObjectNode node = JsonNodeFactory.instance.objectNode()
//        if (!behavior.isEmpty()) {
//            ObjectNode b = JsonNodeFactory.instance.objectNode()
//            behavior.each { trans, behav -> b.set(trans, behaviorToJson(trans)) }
//            node.set("behavior", b)
//        }
//        if (this.value != null)
//            node.put("value", value)
//
//        return node
//    }
//
//    private ObjectNode behaviorToJson(String trans) {
//        ObjectNode node = JsonNodeFactory.instance.objectNode()
//        behavior.get(trans).each { behav -> node.put(behav.toString(), true) }
//        return node
//    }

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

    @Override
    String toString() {
        return attributes as String
    }

    class TaskPair {
        String taskId
        String transition

        TaskPair() {
        }

        TaskPair(String taskId, String transition) {
            this.taskId = taskId
            this.transition = transition
        }
    }
}
