package com.netgrif.application.engine.objects.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.objects.annotations.Indexable;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.annotations.Indexed;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.*;
import com.netgrif.application.engine.objects.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.objects.workflow.service.InitValueExpressionEvaluator;
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
@Indexable
@QueryEntity
public abstract class Case implements Serializable {

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
    private Map<String, Map<String, Boolean>> userRefs;

    @Setter
    private Map<String, Map<String, Boolean>> users;

    @Setter
    private List<String> viewRoles;

    @Setter
    @JsonIgnore
    private List<String> viewUserRefs;

    @Setter
    @JsonIgnore
    private List<String> viewUsers;

    @Setter
    private List<String> negativeViewRoles;

    @Setter
    private List<String> negativeViewUsers;

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
        userRefs = new HashMap<>();
        users = new HashMap<>();
        viewRoles = new LinkedList<>();
        viewUserRefs = new LinkedList<>();
        viewUsers = new LinkedList<>();
        negativeViewRoles = new LinkedList<>();
        negativeViewUsers = new ArrayList<>();
        tags = new HashMap<>();
    }

    public Case(PetriNet petriNet) {
        this();
        this._id = new ProcessResourceId(petriNet.getObjectId());
        petriNetObjectId = petriNet.getObjectId();
        processIdentifier = petriNet.getIdentifier();
        this.petriNet = petriNet;
        activePlaces = petriNet.getActivePlaces();
        immediateDataFields = petriNet.getImmediateFields().stream().map(Field::getStringId).collect(Collectors.toCollection(LinkedHashSet::new));
        visualId = generateVisualId();
        enabledRoles = new HashSet<>(petriNet.getRoles().keySet());
        negativeViewRoles.addAll(petriNet.getNegativeViewRoles());
        icon = petriNet.getIcon();
        userRefs = petriNet.getUserRefs();
        tags = new HashMap<>(petriNet.getTags());

        permissions = petriNet.getPermissions().entrySet().stream()
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
        resolveViewUserRefs();
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
            if (field instanceof UserField) {
                dataField.setChoices(((UserField) field).getRoles().stream().map(I18nString::new).collect(Collectors.toSet()));
            }
            if (field instanceof UserListField) {
                dataField.setChoices(((UserListField) field).getRoles().stream().map(I18nString::new).collect(Collectors.toSet()));
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

    public void addUsers(Set<String> userIds, Map<String, Boolean> permissions) {
        userIds.forEach(userId -> {
            if (users.containsKey(userId) && users.get(userId) != null) {
                compareExistingUserPermissions(userId, new HashMap<>(permissions));
            } else {
                users.put(userId, new HashMap<>(permissions));
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

    public void resolveViewUserRefs() {
        this.viewUserRefs.clear();
        this.userRefs.forEach((userRef, perms) -> {
            if (perms.containsKey(RolePermission.VIEW.getValue()) && perms.get(RolePermission.VIEW.getValue())) {
                viewUserRefs.add(userRef);
            }
        });
    }

    /**
     * Initializes {@link #viewUsers} collection. Any user defined in {@link #users} with permission {@link RolePermission#VIEW}
     * of true value is added to the {@link #viewUsers} collection.
     *
     * @return true if the {@link #viewUsers} was modified, false otherwise
     */
    public boolean resolveViewUsers() {
        AtomicBoolean isModified = new AtomicBoolean(!this.viewUsers.isEmpty());
        this.viewUsers.clear();
        this.users.forEach((user, perms) -> {
            if (perms.containsKey(RolePermission.VIEW.getValue()) && perms.get(RolePermission.VIEW.getValue())) {
                isModified.set(viewUsers.add(user));
            }
        });
        return isModified.get();
    }

    private void compareExistingUserPermissions(String userId, Map<String, Boolean> permissions) {
        permissions.forEach((id, perm) -> {
            if ((users.containsKey(userId) && !users.get(userId).containsKey(id)) || (users.containsKey(userId) && users.get(userId).containsKey(id) && users.get(userId).get(id))) {
                users.get(userId).put(id, perm);
            }
        });
    }
}
