package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

@Document
public class Task {

    @Id
    private ObjectId _id;

    @Getter @Setter
    private String caseId;

    @Setter
    private String transitionId;

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String caseColor;

    @Getter @Setter
    private String caseTitle;

    @Getter
    private String visualId;

    @Getter
    private int priority;

    @Setter
    private Long userId;

    @org.springframework.data.annotation.Transient
    @Getter @Setter
    private User user;

    @DBRef
    @Setter
    private List<Trigger> triggers;

    @Getter @Setter
    private Map<String, Map<String, Boolean>> roles;

    @Getter @Setter
    private LocalDateTime startDate;

    @Getter @Setter
    private LocalDateTime finishDate;

    @Getter @Setter
    private String transactionId;

    public Task() {
        this._id = new ObjectId();
        roles = new HashMap<>();
        this.triggers = new LinkedList<>();
    }

    @JsonIgnore
    public ObjectId getObjectId() {
        return _id;
    }

    public void setObjectId(ObjectId id) {
        this._id = id;
    }

    public String getStringId() {
        return _id.toString();
    }

    public void setVisualId(String petriNetInitials) {
        // TODO: 9.5.2017 bullshit remove now!
        this.visualId = petriNetInitials+"-"+this._id;
    }

    public void setPriority(int priority) {
        this.priority = priority == Priorities.UNDEFINED ? Priorities.LOW : priority;
    }

    public String getTransitionId() {
        return transitionId;
    }

    public void addRole(String roleId, Set<RolePermission> permissions){
        if(roles.containsKey(roleId) && roles.get(roleId) != null)
            roles.get(roleId).putAll(parsePermissionMap(permissions));
        else
            roles.put(roleId,parsePermissionMap(permissions));
    }

    private Map<String, Boolean> parsePermissionMap(Set<RolePermission> permissions){
        Map<String, Boolean> map = new HashMap<>();
        permissions.forEach(perm -> map.put(perm.toString(),true));
        return map;
    }

    @JsonIgnore
    public List<Trigger> getTriggers() {
        return triggers;
    }

    @JsonIgnore
    public Long getUserId() {
        return userId;
    }

    public static class Priorities {
        public static final int HIGH = 3;
        public static final int MEDIUM = 2;
        public static final int LOW = 1;
        public static final int UNDEFINED = 0;

    }

    public enum Type {
        USER,
        AUTO,
        TIME,
        MESSAGE,
    }
}