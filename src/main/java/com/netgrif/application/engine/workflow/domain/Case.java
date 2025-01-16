package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.auth.domain.Author;
import com.netgrif.application.engine.importer.model.CaseEventType;
import com.netgrif.application.engine.importer.model.ProcessEventType;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.workflow.domain.arcs.Arc;
import com.netgrif.application.engine.workflow.domain.arcs.ArcCollection;
import com.netgrif.application.engine.workflow.domain.arcs.PTArc;
import com.netgrif.application.engine.workflow.domain.arcs.TPArc;
import com.netgrif.application.engine.workflow.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.workflow.domain.events.CaseEvent;
import com.netgrif.application.engine.workflow.domain.events.ProcessEvent;
import com.netgrif.application.engine.petrinet.domain.roles.CasePermission;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * todo javadoc
 * */
@Data
@Document
public class Case implements Serializable {

    private static final long serialVersionUID = 3687481049847498422L;

    @Id
    @Setter(AccessLevel.NONE)
    private ObjectId id;
    private ObjectId templateCaseId;
    @NotNull
    @Indexed
    private Scope scope;
    @NotNull
    @Indexed
    private String processIdentifier;
    private Version version;
    @LastModifiedDate
    private LocalDateTime lastModified;
    private LocalDateTime creationDate;
    @NotNull
    private I18nString title;
    private String icon;
    @Indexed
    private Author author;
    private String uriNodeId;
    private UniqueKeyMap<String, String> properties;

    private Workflow workflow;

    @Indexed
    private Map<String, TaskPair> tasks;

    private List<Function> functions;

    private Map<CaseEventType, CaseEvent> caseEvents;
    private Map<ProcessEventType, ProcessEvent> processEvents;

    @JsonIgnore
    @QueryType(PropertyType.NONE)
    private DataSet dataSet;
    /**
     * List of data fields importIds
     */
    @JsonIgnore
    private LinkedHashSet<String> immediateDataFields;
    @Transient
    @QueryType(PropertyType.NONE)
    private List<Field<?>> immediateData;

    @JsonIgnore
    private Map<String, Map<CasePermission, Boolean>> permissions;

    /**
     * todo javadoc
     * */
    public Case(Scope scope) {
        id = new ObjectId();
        this.scope = scope;
        // TODO: release/8.0.0 spring auditing
        creationDate = LocalDateTime.now();
        workflow = new Workflow();
        properties = new UniqueKeyMap<>();
        tasks = new HashMap<>();
        functions = new ArrayList<>();
        caseEvents = new HashMap<>();
        processEvents = new HashMap<>();
        dataSet = new DataSet();
        immediateDataFields = new LinkedHashSet<>();
        immediateData = new ArrayList<>();
        permissions = new HashMap<>();
    }

    public String getStringId() {
        return id.toString();
    }

    public void resolveImmediateDataFields() {
        immediateData = dataSet.getFields().values().stream().filter(Field::isImmediate).collect(Collectors.toList());
        immediateDataFields = immediateData.stream().map(Field::getStringId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public ObjectId getTaskId(String transitionId) {
        if (transitionId == null || !tasks.containsKey(transitionId)) {
            throw new IllegalArgumentException("Case does not have task with transitionId [" + transitionId + "]");
        }
        return tasks.get(transitionId).getTaskId();
    }

    public String getTaskStringId(String transitionId) {
        return getTaskId(transitionId).toString();
    }

    public void addTask(Task task) {
        this.tasks.put(task.getTransitionId(), new TaskPair(task));
    }

    public void removeTasks(List<Task> tasks) {
        tasks.forEach(task ->
                this.tasks.remove(task.getTransitionId())
        );
    }

    public void updateTask(Task task) {
        TaskPair taskPair = tasks.get(task.getTransitionId());
        taskPair.setState(task.getState());
        taskPair.setUserId(task.getAssigneeId());
    }

    /**
     * todo javadoc
     * */
    public void addArc(Arc<?, ?> arc) {
        String transitionId = arc.getTransition().getStringId();
        ArcCollection arcCollection = workflow.getArcs().get(transitionId);
        if (arcCollection == null) {
            arcCollection = new ArcCollection();
            workflow.getArcs().put(transitionId, arcCollection);
        }
        if (arc instanceof PTArc) {
            arcCollection.addInput((PTArc) arc);
        } else {
            arcCollection.addOutput((TPArc) arc);
        }
    }

    /**
     * todo javadoc
     * */
    public Map<String, ArcCollection> getArcs() {
        return workflow.getArcs();
    }

    /**
     * todo javadoc
     * */
    public List<PTArc> getInputArcsOf(String transitionId) {
        if (getArcs().containsKey(transitionId)) {
            return getArcs().get(transitionId).getInput();
        }
        return new LinkedList<>();
    }

    /**
     * todo javadoc
     * */
    public List<TPArc> getOutputArcsOf(String transitionId) {
        if (getArcs().containsKey(transitionId)) {
            return getArcs().get(transitionId).getOutput();
        }
        return new LinkedList<>();
    }

    /**
     * todo javadoc
     * */
    public void addPlace(Place place) {
        workflow.getPlaces().put(place.getStringId(), place);
    }

    /**
     * todo javadoc
     * */
    public Place getPlace(String id) {
        return workflow.getPlaces().get(id);
    }

    /**
     * todo javadoc
     * */
    public Map<String, Place> getPlaces() {
        return workflow.getPlaces();
    }

    /**
     * todo javadoc
     * */
    public Map<String, Integer> getActivePlaces() {
        return workflow.getActivePlaces();
    }

    /**
     * todo javadoc
     * */
    public void setActivePlaces(Map<String, Integer> activePlaces) {
        workflow.setActivePlaces(activePlaces);
    }

    /**
     * todo javadoc
     * */
    public Map<String, Integer> getConsumedTokens() {
        return workflow.getConsumedTokens();
    }

    /**
     * todo javadoc
     * */
    public void addTransition(Transition transition) {
        workflow.getTransitions().put(transition.getStringId(), transition);
    }

    /**
     * todo javadoc
     * */
    public Transition getTransition(String id) {
        // TODO: release/8.0.0 change
        if ("fake".equals(id)) {
            return new Transition();
        }
        return workflow.getTransitions().get(id);
    }

    /**
     * todo javadoc
     * */
    public Map<String, Transition> getTransitions() {
        return workflow.getTransitions();
    }

    /**
     * todo javadoc
     * */
    public void addDataSetField(Field<?> field) {
        this.dataSet.put(field.getStringId(), field);
    }

    /**
     * todo javadoc
     * */
    public void addProcessEvent(ProcessEvent processEvent) {
        processEvents.put(processEvent.getType(), processEvent);
    }

    /**
     * todo javadoc
     * */
    public void addCaseEvent(CaseEvent caseEvent) {
        caseEvents.put(caseEvent.getType(), caseEvent);
    }

    /**
     * todo javadoc
     * */
    public void addFunction(Function function) {
        functions.add(function);
    }

    /**
     * todo javadoc
     * */
    public void addPermission(String actorId, Map<CasePermission, Boolean> permissions) {
        if (this.permissions.containsKey(actorId) && this.permissions.get(actorId) != null) {
            this.permissions.get(actorId).putAll(permissions);
        } else {
            this.permissions.put(actorId, permissions);
        }
    }

    public void addRole(ProcessRole role) {
        // todo 2026 roly
    }

    /**
     * todo javadoc
     * */
    public ProcessRole getRole(String id) {
        // todo 2026 roly
        return null;
    }

    /**
     * todo javadoc
     * */
    public Optional<Field<?>> getField(String id) {
        return Optional.ofNullable(dataSet.get(id));
    }

    /**
     * todo javadoc
     * */
    public void incrementVersion(VersionType byType) {
        this.version.increment(byType);
    }

    /**
     * todo javadoc
     * */
    public void initializeArcs() {
        workflow.getArcs().values().forEach(list -> {
            list.getOutput().forEach(arc -> {
                arc.setSource(getTransition(arc.getSourceId()));
                arc.setDestination(getPlace(arc.getDestinationId()));
            });
            list.getInput().forEach(arc -> {
                arc.setSource(getPlace(arc.getSourceId()));
                arc.setDestination(getTransition(arc.getDestinationId()));
            });
        });
    }



    public List<Action> getPreCreateActions() {
        return getPreCaseActions(CaseEventType.CREATE);
    }

    public List<Action> getPostCreateActions() {
        return getPostCaseActions(CaseEventType.CREATE);
    }

    public List<Action> getPreDeleteActions() {
        return getPreCaseActions(CaseEventType.DELETE);
    }

    public List<Action> getPostDeleteActions() {
        return getPostCaseActions(CaseEventType.DELETE);
    }

    public List<Action> getPreUploadActions() {
        return getPreProcessActions(ProcessEventType.UPLOAD);
    }

    public List<Action> getPostUploadActions() {
        return getPostProcessActions(ProcessEventType.UPLOAD);
    }

    private List<Action> getPreCaseActions(CaseEventType type) {
        if (caseEvents.containsKey(type)) {
            return caseEvents.get(type).getPreActions();
        }
        return new LinkedList<>();
    }

    private List<Action> getPostCaseActions(CaseEventType type) {
        if (caseEvents.containsKey(type)) {
            return caseEvents.get(type).getPostActions();
        }
        return new LinkedList<>();
    }

    private List<Action> getPreProcessActions(ProcessEventType type) {
        if (processEvents.containsKey(type)) {
            return processEvents.get(type).getPreActions();
        }
        return new LinkedList<>();
    }

    private List<Action> getPostProcessActions(ProcessEventType type) {
        if (processEvents.containsKey(type)) {
            return processEvents.get(type).getPostActions();
        }
        return new LinkedList<>();
    }
}