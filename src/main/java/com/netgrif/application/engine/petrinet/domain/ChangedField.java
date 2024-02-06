package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.workflow.domain.Task;
import com.querydsl.core.annotations.QueryExclude;

import java.io.Serializable;
import java.util.*;

@QueryExclude
public class ChangedField implements Serializable {

    private static final long serialVersionUID = 5299918326436828485L;

    public String id;
    public List<TaskPair> changedOn;
    public Map<String, Object> attributes;

    public ChangedField() {
        attributes = new HashMap<>();
        changedOn = new ArrayList<>();
    }

    public ChangedField(String id) {
        this();
        this.id = id;
    }

    public void wasChangedOn(Task task) {
        wasChangedOn(task.getStringId(), task.getTransitionId());
    }

    public void wasChangedOn(String taskId, String transition) {
        if (!isChangedOn(taskId)) {
            changedOn.add(new TaskPair(taskId, transition));
        }
    }

    public boolean isChangedOn(String taskId) {
        return changedOn.stream().anyMatch(taskPair -> Objects.equals(taskPair.taskId, taskId));
    }

    public void addAttribute(String name, Object value) {
        if (value instanceof I18nString)
            attributes.put(name, ((I18nString) value).getDefaultValue());
        else
            attributes.put(name, value);
    }

    public void addBehavior(Map<String, Set<FieldBehavior>> behavior) {
        Map<String, Map<String, Boolean>> behs = new HashMap<>();
        behavior.forEach((trans, fieldBehaviors) -> {
            Map<String, Boolean> b = new HashMap<>();
            fieldBehaviors.forEach(fieldBehavior -> b.put(fieldBehavior.toString(), true));
            behs.put(trans, b);
        });
        attributes.put("behavior", behs);
    }

    public void merge(ChangedField changedField) {
        changedField.changedOn.forEach(taskPair -> wasChangedOn(taskPair.taskId, taskPair.transition));
        this.attributes.putAll(changedField.attributes);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

//    public ObjectNode toJson() {
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

    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        ChangedField that = (ChangedField) o;
        return Objects.equals(id, that.id);
    }

    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return attributes.toString();
    }

    static class TaskPair implements Serializable {

        private static final long serialVersionUID = 5299918326444428485L;

        public String taskId;
        public String transition;

        public TaskPair() {
        }

        public TaskPair(String taskId, String transition) {
            this.taskId = taskId;
            this.transition = transition;
        }
    }
}
