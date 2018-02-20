package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.workflow.domain.triggers.Trigger;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

@Document
public class Task {

    @Id
    private ObjectId _id;

    @Getter @Setter
    private String processId;

    @Getter @Setter
    private String caseId;

    @Setter
    private String transitionId;

    @Getter @Setter
    private I18nString title;

    @Getter @Setter
    private String caseColor;

    @Getter @Setter
    private String caseTitle;

    @Getter @Setter
    private Integer priority;

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
    private Long finishedBy;

    @Getter @Setter
    private String transactionId;

    @Getter @Setter
    private Boolean requiredFilled;

    @Getter @Setter
    @JsonIgnore
    private LinkedHashSet<String> immediateDataFields;

    @Getter @Setter
    @Transient
    private List<Field> immediateData;

    @Setter
    private String icon;

    public Task() {
        this._id = new ObjectId();
        roles = new HashMap<>();
        this.triggers = new LinkedList<>();
        this.immediateDataFields = new LinkedHashSet<>();
        this.immediateData = new ArrayList<>();
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

    public String getTransitionId() {
        return transitionId;
    }

    public String getIcon() {
        return icon;
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

    public void addTrigger(Trigger trigger) {
        triggers.add(trigger);
    }

    public void addImmediateData(Field field){
        this.immediateData.add(field);
    }

    @JsonIgnore
    public Long getUserId() {
        return userId;
    }

    public enum Type {
        USER,
        AUTO,
        TIME,
        MESSAGE,
    }
}