package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.CaseField;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.FieldWithDefault;
import com.netgrif.workflow.petrinet.domain.dataset.UserField;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Document
public class Case {

    @Id
    @Getter
    private ObjectId _id;

    @LastModifiedDate
    @Getter
    @Setter
    private LocalDateTime lastModified;

    @Getter
    private String visualId;

    @NotNull
    @Getter
    @Setter
    private ObjectId petriNetObjectId;

    @JsonIgnore
    @Transient
    @Getter
    @Setter
    private PetriNet petriNet;

    @NotNull
    @Getter
    @Setter
    @Indexed
    private String processIdentifier;

    @org.springframework.data.mongodb.core.mapping.Field("activePlaces")
    @Getter
    @Setter
    @JsonIgnore
    private Map<String, Integer> activePlaces;

    @NotNull
    @Getter
    @Setter
    private String title;

    @Getter
    private String color;

    @Getter
    @Setter
    private String icon;

    @Getter
    @Setter
    private LocalDateTime creationDate;

    @Getter
    @Setter
    @JsonIgnore
    private LinkedHashMap<String, DataField> dataSet;

    /**
     * List of data fields importIds
     */
    @Getter
    @Setter
    @JsonIgnore
    private LinkedHashSet<String> immediateDataFields;

    @Getter
    @Setter
    @Transient
    private List<Field> immediateData;

    @Getter
    @Setter
    @Indexed
    private Author author;

    /**
     * TODO: reset = variable
     */
    @Getter
    @Setter
    private Map<String, Integer> resetArcTokens;

    /**
     * TODO: Indexed?
     */
    @Getter
    @Setter
    @JsonIgnore
    private Set<TaskPair> tasks;

    /**
     * TODO: Indexed?
     */
    @Getter
    @Setter
    @JsonIgnore
    private Set<String> enabledRoles;

    public Case() {
        _id = new ObjectId();
        activePlaces = new HashMap<>();
        dataSet = new LinkedHashMap<>();
        immediateDataFields = new LinkedHashSet<>();
        resetArcTokens = new HashMap<>();
        tasks = new HashSet<>();
        visualId = generateVisualId();
        enabledRoles = new HashSet<>();
    }

    public Case(String title) {
        this();
        this.title = title;
        visualId = generateVisualId();
    }

    public Case(String title, PetriNet petriNet, Map<String, Integer> activePlaces) {
        this(title);
        this.petriNetObjectId = petriNet.getObjectId();
        this.petriNet = petriNet;
        this.activePlaces = activePlaces;
        populateDataSet();
        this.immediateDataFields = petriNet.getImmediateFields().stream().map(Field::getStringId).collect(Collectors.toCollection(LinkedHashSet::new));
        visualId = generateVisualId();
        this.enabledRoles = petriNet.getRoles().keySet();
    }

    public String getStringId() {
        return _id.toString();
    }

    public void setColor(String color) {
        this.color = color == null || color.isEmpty() ? "color-fg-fm-500" : color;
    }

    public boolean hasFieldBehavior(String field, String transition) {
        return this.dataSet.get(field).hasDefinedBehavior(transition);
    }

    private void populateDataSet() {
        petriNet.getDataSet().forEach((key, field) -> {
            if (field instanceof FieldWithDefault) {
                this.dataSet.put(key, new DataField(((FieldWithDefault) field).getDefaultValue()));
            } else {
                this.dataSet.put(key, new DataField());
            }
            if (field instanceof UserField) {
                this.dataSet.get(key).setChoices(((UserField) field).getRoles().stream().map(I18nString::new).collect(Collectors.toSet()));
            }
            if (field instanceof CaseField) {
                this.dataSet.get(key).setValue(new ArrayList<>());
                this.dataSet.get(key).setAllowedNets(((CaseField) field).getAllowedNets());
            }
        });
    }

    private String generateVisualId() {
        SecureRandom random = new SecureRandom();
        int n = _id.getTimestamp() + random.nextInt(99999999);
        if (this.title != null) {
            n += title.length();
        }
        if (this.petriNet != null) {
            return petriNet.getInitials() + "-" + n;
        }
        return n + "";
    }

    public Object getFieldValue(String fieldId) {
        return dataSet.get(fieldId).getValue();
    }

    public boolean addTask(Task task) {
        return this.tasks.add(new TaskPair(task.getStringId(), task.getTransitionId()));
    }

    public boolean removeTask(Task task) {
//        return this.tasks.remove(new TaskPair(task.getStringId(), task.getTransitionId()));
        return this.removeTasks(Collections.singletonList(task));
    }

    public boolean removeTasks(List<Task> tasks) {
//        List<TaskPair> taskPairsToRemove = tasks.stream().map(task -> new TaskPair(task.getStringId(), task.getTransitionId()))
//                .collect(Collectors.toList());
//        return this.tasks.removeAll(taskPairsToRemove);
        int sizeBeforeChange = this.tasks.size();
        Set<String> tasksTransitions = tasks.stream().map(Task::getTransitionId).collect(Collectors.toSet());
        this.tasks = this.tasks.stream().filter(pair -> !tasksTransitions.contains(pair.getTransition())).collect(Collectors.toSet());
        return this.tasks.size() != sizeBeforeChange;
    }

    public Field getField(String id) {
        return petriNet.getDataSet().get(id);
    }

    public DataField getDataField(String id) {
        return dataSet.get(id);
    }

    public String getPetriNetId() {
        return petriNetObjectId.toString();
    }
}