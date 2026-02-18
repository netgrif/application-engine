package com.netgrif.application.engine.objects.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.objects.annotations.EnsureCollection;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.annotations.Indexed;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.*;
import com.netgrif.application.engine.objects.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.objects.workflow.service.InitValueExpressionEvaluator;
import com.netgrif.application.engine.objects.workspace.Workspaceable;
import com.querydsl.core.annotations.QueryEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.io.Serial;
import java.io.Serializable;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Getter
@EnsureCollection
@QueryEntity
public abstract class Case implements Serializable, Workspaceable {

    @Serial
    private static final long serialVersionUID = 3687481049847498422L;

    @Setter
    private ProcessResourceId _id;

    @Setter
    private LocalDateTime lastModified;

    @Setter
    @Indexed
    private String visualId;

    @NotNull
    @Setter
    @Indexed
    private ObjectId petriNetObjectId;

    @JsonIgnore
    @Setter
    private PetriNet petriNet;

    @NotNull
    @Setter
    @Indexed
    private String processIdentifier;

    @Setter
    @Indexed
    private String workspaceId;

    @Setter
    @JsonIgnore
    private Map<String, Integer> activePlaces;

    @NotNull
    @Setter
    @Indexed
    private String title;

    private String color;

    @Setter
    private String icon;

    @Setter
    private LocalDateTime creationDate;

    @Setter
    @JsonIgnore
    private Map<String, DataField> dataSet;

    @Setter
    @JsonIgnore
    private Set<String> immediateDataFields;

    @Setter
    private List<Field<?>> immediateData;

    @Setter
    @Indexed
    private ActorRef author;

    @Setter
    @JsonIgnore
    private Map<String, Integer> consumedTokens;

    @Setter
    private Set<TaskPair> tasks;

    @Setter
    @JsonIgnore
    private Set<String> enabledRoles;

    @Setter
    private Map<String, Map<String, Boolean>> permissions;

    @Setter
    private Map<String, Map<String, Boolean>> actorRefs;

    @Setter
    private Map<String, Map<String, Boolean>> actors;

    @Setter
    private List<String> viewRoles;

    @Setter
    @JsonIgnore
    private List<String> viewActorRefs;

    @Setter
    @JsonIgnore
    private List<String> viewActors;

    @Setter
    private List<String> negativeViewRoles;

    @Setter
    private List<String> negativeViewActors;

    @Setter
    private Map<String, String> tags;

    protected Case() {
        activePlaces = new HashMap<>();
        dataSet = new LinkedHashMap<>();
        immediateDataFields = new LinkedHashSet<>();
        consumedTokens = new HashMap<>();
        tasks = new HashSet<>();
        enabledRoles = new HashSet<>();
        permissions = new HashMap<>();
        actorRefs = new HashMap<>();
        actors = new HashMap<>();
        viewRoles = new LinkedList<>();
        viewActorRefs = new LinkedList<>();
        viewActors = new LinkedList<>();
        negativeViewRoles = new LinkedList<>();
        negativeViewActors = new ArrayList<>();
        tags = new HashMap<>();
    }

    public Case(PetriNet petriNet) {
        this();
        this._id = new ProcessResourceId(petriNet.getObjectId());
        this.petriNetObjectId = petriNet.getObjectId();
        this.processIdentifier = petriNet.getIdentifier();
        this.petriNet = petriNet;
        this.activePlaces = petriNet.getActivePlaces();
        this.immediateDataFields = petriNet.getImmediateFields().stream().map(Field::getStringId).collect(Collectors.toCollection(LinkedHashSet::new));
        this.visualId = generateVisualId();
        this.enabledRoles = new HashSet<>(petriNet.getRoles().keySet());
        this.negativeViewRoles.addAll(petriNet.getNegativeViewRoles());
        this.icon = petriNet.getIcon();
        this.actorRefs = petriNet.getActorRefs();
        this.tags = new HashMap<>(petriNet.getTags());
        this.workspaceId = petriNet.getWorkspaceId();

        this.permissions = petriNet.getPermissions().entrySet().stream()
                .filter(role -> role.getValue().containsKey("delete") || role.getValue().containsKey("view"))
                .map(role -> {
                    Map<String, Boolean> permissionMap = new HashMap<>();
                    if (role.getValue().containsKey("delete"))
                        permissionMap.put("delete", role.getValue().get("delete"));
                    if (role.getValue().containsKey("view")) {
                        permissionMap.put("view", role.getValue().get("view"));
                    }
                    return new AbstractMap.SimpleEntry<>(role.getKey(), permissionMap);
                })
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        resolveViewRoles();
        resolveViewActorRefs();
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

    public void populateDataSet(InitValueExpressionEvaluator initValueExpressionEvaluator, Map<String, String> params) {
        List<Field<?>> dynamicInitFields = new LinkedList<>();
        List<MapOptionsField<I18nString, ?>> dynamicOptionsFields = new LinkedList<>();
        List<ChoiceField<?>> dynamicChoicesFields = new LinkedList<>();
        petriNet.getDataSet().forEach((key, field) -> {
            DataField dataField;
            if (field.isDynamicDefaultValue()) {
                dynamicInitFields.add(field);
                dataField = new DataField();
                this.dataSet.put(key, dataField);
            } else {
                dataField = new DataField(field.getDefaultValue());
                this.dataSet.put(key, dataField);
            }
            if (field.getComponent() != null) {
                dataField.setComponent(field.getComponent());
            }
            if (field instanceof ActorField) {
                dataField.setChoices(((ActorField) field).getRoles().stream().map(I18nString::new).collect(Collectors.toSet()));
            }
            if (field instanceof ActorListField) {
                dataField.setChoices(((ActorListField) field).getRoles().stream().map(I18nString::new).collect(Collectors.toSet()));
            }
            if (field instanceof FieldWithAllowedNets) {
                dataField.setAllowedNets(((FieldWithAllowedNets) field).getAllowedNets());
            }
            if (field instanceof FilterField) {
                dataField.setFilterMetadata(((FilterField) field).getFilterMetadata());
            }
            if (field instanceof MapOptionsField && ((MapOptionsField) field).isDynamic()) {
                dynamicOptionsFields.add((MapOptionsField<I18nString, ?>) field);
            }
            if (field instanceof ChoiceField && ((ChoiceField) field).isDynamic()) {
                dynamicChoicesFields.add((ChoiceField<?>) field);
            }
        });
        dynamicInitFields.forEach(field -> this.dataSet.get(field.getImportId()).setValue(initValueExpressionEvaluator.evaluate(this, field, params)));
        dynamicChoicesFields.forEach(field -> this.dataSet.get(field.getImportId()).setChoices(initValueExpressionEvaluator.evaluateChoices(this, field, params)));
        dynamicOptionsFields.forEach(field -> this.dataSet.get(field.getImportId()).setOptions(initValueExpressionEvaluator.evaluateOptions(this, field, params)));
        populateDataSetBehaviorAndComponents();
    }

    private void populateDataSetBehaviorAndComponents() {
        petriNet.getTransitions().forEach((transitionKey, transitionValue) -> {
            transitionValue.getDataSet().forEach((dataKey, dataValue) -> {
                getDataSet().get(dataKey).addBehavior(transitionKey, new HashSet<>(dataValue.getBehavior()));
                if (dataValue.getComponent() != null) {
                    getDataSet().get(dataKey).addDataRefComponent(transitionKey, dataValue.getComponent());
                }
            });
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
        return this.removeTasks(Collections.singletonList(task));
    }

    public boolean removeTasks(List<Task> tasks) {
        int sizeBeforeChange = this.tasks.size();
        Set<String> tasksTransitions = tasks.stream().map(Task::getTransitionId).collect(Collectors.toSet());
        this.tasks = this.tasks.stream().filter(pair -> !tasksTransitions.contains(pair.getTransition())).collect(Collectors.toSet());
        return this.tasks.size() != sizeBeforeChange;
    }

    public Field<?> getField(String id) {
        return petriNet.getDataSet().get(id);
    }

    public DataField getDataField(String id) {
        return dataSet.get(id);
    }

    public String getPetriNetId() {
        return petriNetObjectId.toString();
    }

    public void addActors(Set<String> actorIds, Map<String, Boolean> permissions) {
        actorIds.forEach(actorId -> {
            if (actors.containsKey(actorId) && actors.get(actorId) != null) {
                compareExistingActorPermissions(actorId, new HashMap<>(permissions));
            } else {
                actors.put(actorId, new HashMap<>(permissions));
            }
        });
    }

    public void resolveViewRoles() {
        this.viewRoles.clear();
        this.permissions.forEach((role, perms) -> {
            if (perms.containsKey(RolePermission.VIEW.getValue()) && perms.get(RolePermission.VIEW.getValue())) {
                viewRoles.add(role);
            }
        });
    }

    public void resolveViewActorRefs() {
        this.viewActorRefs.clear();
        this.actorRefs.forEach((actorRef, perms) -> {
            if (perms.containsKey(RolePermission.VIEW.getValue()) && perms.get(RolePermission.VIEW.getValue())) {
                viewActorRefs.add(actorRef);
            }
        });
    }

    /**
     * Initializes {@link #viewActors} collection. Any actor defined in {@link #actors} with permission {@link RolePermission#VIEW}
     * of true value is added to the {@link #viewActors} collection.
     *
     * @return true if the {@link #viewActors} was modified, false otherwise
     */
    public boolean resolveViewActors() {
        AtomicBoolean isModified = new AtomicBoolean(!this.viewActors.isEmpty());
        this.viewActors.clear();
        this.actors.forEach((actor, perms) -> {
            if (perms.containsKey(RolePermission.VIEW.getValue()) && perms.get(RolePermission.VIEW.getValue())) {
                isModified.set(viewActors.add(actor));
            }
        });
        return isModified.get();
    }

    // todo javadoc
    public Optional<String> findTaskId(String transId) {
        return this.tasks.stream()
                .filter(taskPair -> taskPair.getTransition().equals(transId))
                .map(TaskPair::getTask)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private void compareExistingActorPermissions(String actorId, Map<String, Boolean> permissions) {
        permissions.forEach((permType, permValue) -> {
            if ((actors.containsKey(actorId) && !actors.get(actorId).containsKey(permType))
                    || (actors.containsKey(actorId) && actors.get(actorId).containsKey(permType) && actors.get(actorId).get(permType))) {
                actors.get(actorId).put(permType, permValue);
            }
        });
    }
}
