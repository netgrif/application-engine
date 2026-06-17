package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

@Data
public class ChangedField implements Serializable {

    @Serial
    private static final long serialVersionUID = 5299918326436828485L;

    private String id;
    private List<TaskPair> changedOn;
    @Getter
    private Map<String, Object> attributes;

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

    @NoArgsConstructor
    static class TaskPair implements Serializable {

        @Serial
        private static final long serialVersionUID = 5299918326444428485L;

        public String taskId;
        public String transition;

        public TaskPair(String taskId, String transition) {
            this.taskId = taskId;
            this.transition = transition;
        }
    }
}
