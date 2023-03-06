package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import com.netgrif.application.engine.petrinet.domain.layout.TaskLayout;
import com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy;
import com.netgrif.application.engine.petrinet.domain.policies.DataFocusPolicy;
import com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy;
import com.netgrif.application.engine.petrinet.domain.roles.AssignedUserPermission;
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

@Document
@AllArgsConstructor
@Getter
@Setter
@Builder(builderMethodName = "with")
public class Task {

    @Id
    @Builder.Default
    private ObjectId _id = new ObjectId();

    @Indexed
    private String processId;

    @Indexed
    private String caseId;

    @Indexed
    private String transitionId;

    @QueryType(PropertyType.NONE)
    private TaskLayout layout;

    private I18nString title;

    private String caseColor;

    private String caseTitle;

    private Integer priority;

    @Indexed
    private String userId;

    @org.springframework.data.annotation.Transient
    private IUser user;

    @DBRef
    @Builder.Default
    private List<Trigger> triggers = new LinkedList<>();

    /**
     * Role ObjectId : [ RolePermission, true/false ]
     */
    @Builder.Default
    private Map<String, Map<RolePermission, Boolean>> roles = new HashMap<>();

    @Builder.Default
    private Map<String, Map<RolePermission, Boolean>> userRefs = new HashMap<>();

    @Builder.Default
    private Map<String, Map<RolePermission, Boolean>> users = new HashMap<>();

    @Builder.Default
    private List<String> viewRoles = new LinkedList<>();

    @Builder.Default
    private List<String> viewUserRefs = new LinkedList<>();

    @Builder.Default
    private List<String> viewUsers = new LinkedList<>();

    @Builder.Default
    private List<String> negativeViewRoles = new LinkedList<>();

    @Builder.Default
    private List<String> negativeViewUsers = new LinkedList<>();

    private LocalDateTime startDate;

    private LocalDateTime finishDate;

    private String finishedBy;

    private String transactionId;

    /**
     * transient
     */
    private Boolean requiredFilled;

    // TODO: NAE-1645 remove, dynamically load from dataSet
    @Getter
    @Setter
    @JsonIgnore
    @Builder.Default
    private LinkedHashSet<String> immediateDataFields = new LinkedHashSet<>();
    @Getter
    @Setter
    @Transient
    @Builder.Default
    private List<Field<?>> immediateData = new LinkedList<>();

    private String icon;

    @Builder.Default
    private AssignPolicy assignPolicy = AssignPolicy.MANUAL;

    @Builder.Default
    private DataFocusPolicy dataFocusPolicy = DataFocusPolicy.MANUAL;

    @Builder.Default
    private FinishPolicy finishPolicy = FinishPolicy.MANUAL;

    @Builder.Default
    private Map<EventType, I18nString> eventTitles = new HashMap<>();

    @Builder.Default
    private Map<AssignedUserPermission, Boolean> assignedUserPolicy = new HashMap<>();

    private Map<String, Integer> consumedTokens = new HashMap<>();

    public Task() {
    }

    @JsonIgnore
    public ObjectId getObjectId() {
        return _id;
    }

    public String getStringId() {
        return _id.toString();
    }

    public String getTransitionId() {
        return transitionId;
    }

    public String getIcon() {
        return icon;
    }

    public void addRole(String roleId, Map<RolePermission, Boolean> permissions) {
        if (roles.containsKey(roleId) && roles.get(roleId) != null) {
            roles.get(roleId).putAll(permissions);
        } else {
            roles.put(roleId, permissions);
        }
    }

    public void addNegativeViewRole(String roleId) {
        negativeViewRoles.add(roleId);
    }

    public void addUserRef(String userRefId, Map<RolePermission, Boolean> permissions) {
        userRefs.put(userRefId, permissions);
    }

    public void addUsers(Set<String> userIds, Map<RolePermission, Boolean> permissions) {
        userIds.forEach(userId -> {
            if (users.containsKey(userId) && users.get(userId) != null) {
                compareExistingUserPermissions(userId, new HashMap<>(permissions));
            } else {
                users.put(userId, new HashMap<>(permissions));
            }
        });
    }

    public void addAssignedUserPolicy(Map<AssignedUserPermission, Boolean> assignedUser) {
        assignedUserPolicy.putAll(assignedUser);
    }

    @JsonIgnore
    public List<Trigger> getTriggers() {
        return triggers;
    }

    public void addTrigger(Trigger trigger) {
        triggers.add(trigger);
    }

    public void addEventTitle(EventType type, I18nString title) {
        if (type == null || title == null)
            return;
        eventTitles.put(type, title);
    }

    @JsonIgnore
    public String getUserId() {
        return userId;
    }

    public enum Type {
        USER,
        AUTO,
        TIME,
        MESSAGE,
    }

    public void resolveViewRoles() {
        this.viewRoles.clear();
        this.roles.forEach((role, perms) -> {
            if (perms.containsKey(RolePermission.VIEW) && perms.get(RolePermission.VIEW)) {
                viewRoles.add(role);
            }
        });
    }

    public void resolveViewUserRefs() {
        this.viewUserRefs.clear();
        this.userRefs.forEach((userRef, perms) -> {
            if (perms.containsKey(RolePermission.VIEW) && perms.get(RolePermission.VIEW)) {
                viewUserRefs.add(userRef);
            }
        });
    }

    public void resolveViewUsers() {
        this.viewUsers.clear();
        this.users.forEach((role, perms) -> {
            if (perms.containsKey(RolePermission.VIEW) && perms.get(RolePermission.VIEW)) {
                viewUsers.add(role);
            }
        });
    }

    private void compareExistingUserPermissions(String userId, Map<RolePermission, Boolean> permissions) {
        permissions.forEach((id, perm) -> {
            if ((users.containsKey(userId) && !users.get(userId).containsKey(id)) || (users.containsKey(userId) && users.get(userId).containsKey(id) && users.get(userId).get(id))) {
                users.get(userId).put(id, perm);
            }
        });
    }
}