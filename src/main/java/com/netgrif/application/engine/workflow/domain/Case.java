package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.auth.domain.Author;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission;
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
    private ObjectId id;
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
    private Map<String, TaskPair> tasks = new HashMap<>();
    // TODO: release/7.0.0 review json ignore and refactor to common Permission class
    private Set<String> enabledRoles = new HashSet<>();
    //@JsonIgnore TODO: NAE-1866 refactor permission to be used only on backend
    private Map<String, Map<ProcessRolePermission, Boolean>> permissions = new HashMap<>();
    //@JsonIgnore TODO: NAE-1866 refactor permission to be used only on backend
    private Map<String, Map<ProcessRolePermission, Boolean>> userRefs = new HashMap<>();
    //@JsonIgnore TODO: NAE-1866 refactor permission to be used only on backend
    private Map<String, Map<ProcessRolePermission, Boolean>> users = new HashMap<>();
    //@JsonIgnore TODO: NAE-1866 refactor permission to be used only on backend
    private List<String> viewRoles = new ArrayList<>();
    //@JsonIgnore TODO: NAE-1866 refactor permission to be used only on backend
    private List<String> viewUserRefs = new ArrayList<>();
    //@JsonIgnore TODO: NAE-1866 refactor permission to be used only on backend
    private List<String> viewUsers = new ArrayList<>();
    //@JsonIgnore TODO: NAE-1866 refactor permission to be used only on backend
    private List<String> negativeViewRoles = new ArrayList<>();
    //@JsonIgnore TODO: NAE-1866 refactor permission to be used only on backend
    private List<String> negativeViewUsers = new ArrayList<>();

    public Case() {
        id = new ObjectId();
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
        return id.toString();
    }

    public void resolveImmediateDataFields() {
        immediateData = dataSet.getFields().values().stream().filter(Field::isImmediate).collect(Collectors.toList());
        immediateDataFields = immediateData.stream().map(Field::getStringId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setColor(String color) {
        // TODO: release/7.0.0
        this.color = color == null || color.isEmpty() ? "color-fg-fm-500" : color;
    }

    private String generateVisualId() {
        SecureRandom random = new SecureRandom();
        int n = id.getTimestamp() + random.nextInt(99999999);
        if (this.title != null) {
            n += title.length();
        }
        if (this.petriNet != null) {
            return petriNet.getInitials() + "-" + n;
        }
        return n + "";
    }

    public ObjectId getTaskId(String transitionId) {
        if (transitionId == null) {
            throw new IllegalArgumentException("TransitionId cannot be null");
        }
        TaskPair taskPair = tasks.get(transitionId);
        if (taskPair == null) {
            throw new IllegalArgumentException("Case does not have task with transitionId [" + transitionId + "]");
        }
        return taskPair.getTaskId();
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