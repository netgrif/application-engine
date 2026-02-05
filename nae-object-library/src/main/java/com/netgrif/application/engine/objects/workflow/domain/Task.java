package com.netgrif.application.engine.objects.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.objects.annotations.Indexable;
import com.netgrif.application.engine.objects.annotations.Indexed;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.petrinet.domain.layout.TaskLayout;
import com.netgrif.application.engine.objects.petrinet.domain.policies.AssignPolicy;
import com.netgrif.application.engine.objects.petrinet.domain.policies.DataFocusPolicy;
import com.netgrif.application.engine.objects.petrinet.domain.policies.FinishPolicy;
import com.netgrif.application.engine.objects.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.objects.workflow.domain.triggers.Trigger;
import com.querydsl.core.annotations.QueryEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@QueryEntity
@Indexable
@AllArgsConstructor
public abstract class Task implements Serializable {

    @Serial
    private static final long serialVersionUID = -7112277728921547546L;

    private ProcessResourceId _id;

    @Getter
    @Indexed
    private String processId;

    @Getter
    @Setter
    @Indexed
    private String caseId;

    @Getter
    @Setter
    @Indexed
    private String transitionId;

    @Getter
    @Setter
    private TaskLayout layout;

    @Getter
    @Setter
    private I18nString title;

    @Getter
    @Setter
    private String caseColor;

    @Getter
    @Setter
    private String caseTitle;

    @Getter
    @Setter
    private Integer priority;

    @Getter
    @Setter
    @Indexed
    private ActorRef assignee;

    @Getter
    @Setter
    private AbstractUser user;

    @Setter
    private List<Trigger> triggers = new LinkedList<>();

    @Getter
    @Setter
    private Map<String, Map<String, Boolean>> roles = new HashMap<>();

    @Getter
    @Setter
    private Map<String, Map<String, Boolean>> userRefs = new HashMap<>();

    @Getter
    @Setter
    private Map<String, Map<String, Boolean>> users = new HashMap<>();

    @Setter
    private List<String> viewRoles = new LinkedList<>();

    @Setter
    private List<String> viewUserRefs = new LinkedList<>();

    @Setter
    private List<String> viewUsers = new LinkedList<>();

    @Setter
    private List<String> negativeViewRoles = new LinkedList<>();

    @Setter
    private List<String> negativeViewUsers = new LinkedList<>();

    @Getter
    @Setter
    private LocalDateTime startDate;

    @Getter
    @Setter
    private LocalDateTime finishDate;

    @Getter
    @Setter
    private String finishedBy;

    @Getter
    @Setter
    private String transactionId;

    @Getter
    @Setter
    private Boolean requiredFilled;

    @Getter
    @Setter
    @JsonIgnore
    private LinkedHashSet<String> immediateDataFields = new LinkedHashSet<>();

    @Getter
    @Setter
    private List<Field<?>> immediateData = new LinkedList<>();

    @Getter
    @Setter
    private String icon;

    @Getter
    @Setter
    private AssignPolicy assignPolicy = AssignPolicy.MANUAL;

    @Getter
    @Setter
    private DataFocusPolicy dataFocusPolicy = DataFocusPolicy.MANUAL;

    @Getter
    @Setter
    private FinishPolicy finishPolicy = FinishPolicy.MANUAL;

    @Getter
    @Setter
    private Map<EventType, I18nString> eventTitles = new HashMap<>();

    @Getter
    @Setter
    private Map<String, Boolean> assignedUserPolicy = new HashMap<>();

    private Map<String, Integer> consumedTokens = new HashMap<>();

    @Getter
    @Setter
    private Map<String, String> tags = new HashMap<>();

    public Task() {
        if (this.processId != null && !this.processId.isEmpty()) {
            this._id = new ProcessResourceId(new ObjectId(this.processId));
        }
    }

    @JsonIgnore
    public ProcessResourceId getObjectId() {
        return _id;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
        if (processId != null && !processId.isEmpty()) {
            this._id = new ProcessResourceId(new ObjectId(processId));
        }
    }

    public ProcessResourceId get_id() {
        if (this._id == null) {
            this._id = this.processId != null && !this.processId.isEmpty()
                    ? new ProcessResourceId(new ObjectId(this.processId))
                    : new ProcessResourceId();
        }
        return this._id;
    }

    public String getStringId() {
        return get_id().toString();
    }

    public List<String> getViewRoles() {
        if (viewRoles == null) {
            viewRoles = new LinkedList<>();
        }
        return viewRoles;
    }

    public List<String> getViewUserRefs() {
        if (viewUserRefs == null) {
            viewUserRefs = new LinkedList<>();
        }
        return viewUserRefs;
    }

    public List<String> getViewUsers() {
        if (viewUsers == null) {
            viewUsers = new LinkedList<>();
        }
        return viewUsers;
    }

    public List<String> getNegativeViewRoles() {
        if (negativeViewRoles == null) {
            negativeViewRoles = new LinkedList<>();
        }
        return negativeViewRoles;
    }

    public List<String> getNegativeViewUsers() {
        if (negativeViewUsers == null) {
            negativeViewUsers = new LinkedList<>();
        }
        return negativeViewUsers;
    }

    public void addRole(String roleId, Map<String, Boolean> permissions) {
        if (roles.containsKey(roleId) && roles.get(roleId) != null)
            roles.get(roleId).putAll(permissions);
        else
            roles.put(roleId, permissions);
    }

    public void addNegativeViewRole(String roleId) {
        negativeViewRoles.add(roleId);
    }

    public void addUserRef(String userRefId, Map<String, Boolean> permissions) {
        userRefs.put(userRefId, permissions);
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

    public void addAssignedUserPolicy(Map<String, Boolean> assignedUser) {
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
        return assignee != null ? assignee.getId() : null;
    }

    @JsonIgnore
    public String getUserRealmId() {
        return assignee != null ? assignee.getRealmId() : null;
    }

    public String getTranslatedEventTitle(EventType assign, Locale locale) {
        if (eventTitles == null || !eventTitles.containsKey(assign))
            return null;
        return eventTitles.get(assign).getTranslation(locale);
    }

    public void resolveViewRoles() {
        getViewRoles();
        this.viewRoles.clear();
        this.roles.forEach((role, perms) -> {
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

    /**
     * Initializes {@link #viewUsers} collection. Any user defined in {@link #users} with permission {@link RolePermission#VIEW}
     * of true value is added to the {@link #viewUsers} collection.
     *
     * @return true if the {@link #viewUsers} was modified, false otherwise
     */
    public boolean resolveViewUsers() {
        getViewUsers();
        AtomicBoolean isModified = new AtomicBoolean(!this.viewUsers.isEmpty());
        this.viewUsers.clear();
        this.users.forEach((role, perms) -> {
            if (perms.containsKey(RolePermission.VIEW.getValue()) && perms.get(RolePermission.VIEW.getValue())) {
                isModified.set(viewUsers.add(role));
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

    public enum Type {
        USER,
        AUTO,
        TIME,
        MESSAGE,
    }

}
