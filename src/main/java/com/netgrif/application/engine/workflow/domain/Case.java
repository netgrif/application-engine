package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.auth.domain.Author;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.AccessLevel;
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
import java.util.stream.Collectors;

@Document
@Getter
@Setter
public class Case {

    @Id
    @Setter(AccessLevel.NONE)
    private ObjectId _id;
    private String uriNodeId;
    @LastModifiedDate
    private LocalDateTime lastModified;
    @Indexed
    @Setter(AccessLevel.NONE)
    private String visualId;
    @NotNull
    private ObjectId petriNetObjectId;
    @JsonIgnore
    @Transient
    @QueryType(PropertyType.NONE)
    private PetriNet petriNet;
    @NotNull
    @Indexed
    private String processIdentifier;
    @org.springframework.data.mongodb.core.mapping.Field("activePlaces")
    @JsonIgnore
    private Map<String, Integer> activePlaces = new HashMap<>();
    @NotNull
    private String title;
    private String color;
    private String icon;
    private LocalDateTime creationDate;
    @JsonIgnore
    @QueryType(PropertyType.NONE)
    private DataSet dataSet = new DataSet();
    /**
     * List of data fields importIds
     */
    @JsonIgnore
    private LinkedHashSet<String> immediateDataFields = new LinkedHashSet<>();
    @Transient
    @QueryType(PropertyType.NONE)
    private List<Field<?>> immediateData = new ArrayList<>();
    @Indexed
    private Author author;
    @JsonIgnore
    @QueryType(PropertyType.NONE)
    private Map<String, Integer> consumedTokens = new HashMap<>();
    @Indexed
    private Set<TaskPair> tasks = new HashSet<>();
    // TODO: NAE-1645 review json ignore and refactor to common Permission class
    @JsonIgnore
    private Set<String> enabledRoles = new HashSet<>();
    @JsonIgnore
    private Map<String, Map<ProcessRolePermission, Boolean>> permissions = new HashMap<>();
    @JsonIgnore
    private Map<String, Map<ProcessRolePermission, Boolean>> userRefs = new HashMap<>();
    @JsonIgnore
    private Map<String, Map<ProcessRolePermission, Boolean>> users = new HashMap<>();
    @JsonIgnore
    private List<String> viewRoles = new ArrayList<>();
    @JsonIgnore
    private List<String> viewUserRefs = new ArrayList<>();
    @JsonIgnore
    private List<String> viewUsers = new ArrayList<>();
    @JsonIgnore
    private List<String> negativeViewRoles = new ArrayList<>();
    @JsonIgnore
    private List<String> negativeViewUsers = new ArrayList<>();

    public Case() {
        _id = new ObjectId();
    }

    public Case(PetriNet petriNet) {
        this();
        this.petriNet = petriNet;
        petriNetObjectId = petriNet.getObjectId();
        processIdentifier = petriNet.getIdentifier();
        activePlaces = petriNet.getActivePlaces();
        visualId = generateVisualId();
        enabledRoles = petriNet.getRoles().keySet();
        negativeViewRoles.addAll(petriNet.getNegativeViewRoles());
        icon = petriNet.getIcon();
        userRefs = petriNet.getUserRefs();

        permissions = petriNet.getPermissions().entrySet().stream()
                .filter(role -> role.getValue().containsKey(ProcessRolePermission.DELETE) || role.getValue().containsKey(ProcessRolePermission.VIEW))
                .map(role -> {
                    Map<ProcessRolePermission, Boolean> permissionMap = new HashMap<>();
                    if (role.getValue().containsKey(ProcessRolePermission.DELETE))
                        permissionMap.put(ProcessRolePermission.DELETE, role.getValue().get(ProcessRolePermission.DELETE));
                    if (role.getValue().containsKey(ProcessRolePermission.VIEW)) {
                        permissionMap.put(ProcessRolePermission.VIEW, role.getValue().get(ProcessRolePermission.VIEW));
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

    public void resolveImmediateDataFields() {
        immediateData = dataSet.getFields().values().stream().filter(Field::isImmediate).collect(Collectors.toList());
        immediateDataFields = immediateData.stream().map(Field::getStringId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setColor(String color) {
        // TODO: NAE-1645
        this.color = color == null || color.isEmpty() ? "color-fg-fm-500" : color;
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

    public Field<?> getDataField(String id) {
        return dataSet.get(id);
    }

    public String getPetriNetId() {
        return petriNetObjectId.toString();
    }

    public void addUsers(Set<String> userIds, Map<ProcessRolePermission, Boolean> permissions) {
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
            if (perms.containsKey(ProcessRolePermission.VIEW) && perms.get(ProcessRolePermission.VIEW)) {
                viewRoles.add(role);
            }
        });
    }

    public void resolveViewUserRefs() {
        this.viewUserRefs.clear();
        this.userRefs.forEach((userRef, perms) -> {
            if (perms.containsKey(ProcessRolePermission.VIEW) && perms.get(ProcessRolePermission.VIEW)) {
                viewUserRefs.add(userRef);
            }
        });
    }

    public void resolveViewUsers() {
        this.viewUsers.clear();
        this.users.forEach((user, perms) -> {
            if (perms.containsKey(ProcessRolePermission.VIEW) && perms.get(ProcessRolePermission.VIEW)) {
                viewUsers.add(user);
            }
        });
    }

    private void compareExistingUserPermissions(String userId, Map<ProcessRolePermission, Boolean> permissions) {
        permissions.forEach((id, perm) -> {
            if ((users.containsKey(userId) && !users.get(userId).containsKey(id)) || (users.containsKey(userId) && users.get(userId).containsKey(id) && users.get(userId).get(id))) {
                users.get(userId).put(id, perm);
            }
        });
    }
}