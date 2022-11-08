package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.auth.domain.Author;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Builder;
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
public class Case {

    @Id
    @Getter
    private ObjectId _id;

    @LastModifiedDate
    @Getter
    @Setter
    private LocalDateTime lastModified;

    @Getter
    @Indexed
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
    @QueryType(PropertyType.NONE)
    private DataSet dataSet;

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

    @Getter
    @Setter
    @JsonIgnore
    private Map<String, Integer> consumedTokens;

    @Getter
    @Setter
    @Indexed
    private Set<TaskPair> tasks;

    @Getter
    @Setter
    @JsonIgnore
    private Set<String> enabledRoles;

    @Getter
    @Setter
    private Map<String, Map<String, Boolean>> permissions;

    @Getter
    @Setter
    @Builder.Default
    private Map<String, Map<String, Boolean>> userRefs = new HashMap<>();

    @Getter
    @Setter
    @Builder.Default
    private Map<String, Map<String, Boolean>> users = new HashMap<>();

    @Getter
    @Setter
    private List<String> viewRoles;

    @Getter
    @Setter
    @JsonIgnore
    private List<String> viewUserRefs;

    @Getter
    @Setter
    @JsonIgnore
    private List<String> viewUsers;

    @Getter
    @Setter
    private List<String> negativeViewRoles;

    @Getter
    @Setter
    private List<String> negativeViewUsers;

    public Case(PetriNet petriNet) {
        _id = new ObjectId();
        dataSet = new DataSet();
        consumedTokens = new HashMap<>();
        tasks = new HashSet<>();
        users = new HashMap<>();
        viewRoles = new LinkedList<>();
        viewUserRefs = new LinkedList<>();
        viewUsers = new LinkedList<>();
        negativeViewUsers = new ArrayList<>();

        this.petriNet = petriNet;
        petriNetObjectId = petriNet.getObjectId();
        processIdentifier = petriNet.getIdentifier();
        activePlaces = petriNet.getActivePlaces();
        immediateDataFields = petriNet.getImmediateFields().stream().map(Field::getStringId).collect(Collectors.toCollection(LinkedHashSet::new));
        visualId = generateVisualId();
        enabledRoles = petriNet.getRoles().keySet();
        negativeViewRoles.addAll(petriNet.getNegativeViewRoles());
        icon = petriNet.getIcon();
        userRefs = petriNet.getUserRefs();

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
        return this.dataSet.get(field).getBehaviors().get(transition) != null;
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

    public Field getField(String id) {
        return petriNet.getDataSet().get(id);
    }

    public Field getDataField(String id) {
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
        getViewRoles();
        this.viewRoles.clear();
        this.permissions.forEach((role, perms) -> {
            if (perms.containsKey(RolePermission.VIEW.getValue()) && perms.get(RolePermission.VIEW.getValue())) {
                viewRoles.add(role);
            }
        });
    }

    public void resolveViewUserRefs() {
        getViewUserRefs();
        this.viewUserRefs.clear();
        this.userRefs.forEach((userRef, perms) -> {
            if (perms.containsKey(RolePermission.VIEW.getValue()) && perms.get(RolePermission.VIEW.getValue())) {
                viewUserRefs.add(userRef);
            }
        });
    }

    public void resolveViewUsers() {
        getViewUsers();
        this.viewUsers.clear();
        this.users.forEach((user, perms) -> {
            if (perms.containsKey(RolePermission.VIEW.getValue()) && perms.get(RolePermission.VIEW.getValue())) {
                viewUsers.add(user);
            }
        });
    }

    private void compareExistingUserPermissions(String userId, Map<String, Boolean> permissions) {
        permissions.forEach((id, perm) -> {
            if ((users.containsKey(userId) && !users.get(userId).containsKey(id)) || (users.containsKey(userId) && users.get(userId).containsKey(id) && users.get(userId).get(id))) {
                users.get(userId).put(id, perm);
            }
        });
    }
}