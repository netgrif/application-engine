package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.importer.model.TriggerType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy;
import com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy;
import com.netgrif.application.engine.petrinet.domain.roles.TaskPermission;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static com.netgrif.application.engine.workflow.domain.State.DISABLED;

@Document
@AllArgsConstructor
@Getter
@Setter
@Builder(builderMethodName = "with")
public class Task implements Serializable {

    private static final long serialVersionUID = -7112277728921547546L;

    @Id
    @Builder.Default
    private ObjectId id = new ObjectId();
    @Indexed
    private String processId;
    @Indexed
    private String caseId;
    @Getter
    @Indexed
    private String transitionId;
    @Indexed
    private State state = DISABLED;
    @Indexed
    private String assigneeId;

    private I18nString title;
    @Getter
    private String icon;

    @Builder.Default
    private AssignPolicy assignPolicy = AssignPolicy.MANUAL;
    @Builder.Default
    private FinishPolicy finishPolicy = FinishPolicy.MANUAL;


    @Builder.Default
    private List<Trigger> triggers = new LinkedList<>();

    /**
     * Role ObjectId : [ RolePermission, true/false ]
     */
    @Builder.Default
    private Map<String, Map<TaskPermission, Boolean>> permissions = new HashMap<>();

    private LocalDateTime lastAssigned;
    private LocalDateTime lastFinished;
    private String finishedBy;

    // TODO: release/8.0.0 remove, dynamically load from dataSet
    @JsonIgnore
    @Builder.Default
    private LinkedHashSet<String> immediateDataFields = new LinkedHashSet<>();
    @Transient
    @Builder.Default
    private List<Field<?>> immediateData = new LinkedList<>();

    private Map<String, Integer> consumedTokens = new HashMap<>();
    @Builder.Default
    private Map<String, String> properties = new HashMap<>();

    public Task() {
    }

    @JsonIgnore
    public ObjectId getObjectId() {
        return id;
    }

    public String getStringId() {
        return id.toString();
    }

    public void addRole(String roleId, Map<TaskPermission, Boolean> permissions) {
        if (this.permissions.containsKey(roleId) && this.permissions.get(roleId) != null) {
            this.permissions.get(roleId).putAll(permissions);
        } else {
            this.permissions.put(roleId, permissions);
        }
    }

    @JsonIgnore
    public List<Trigger> getTriggers() {
        return triggers;
    }

    public void addTrigger(Trigger trigger) {
        triggers.add(trigger);
    }

    // TODO: release/8.0.0
    /*public String getTranslatedEventTitle(EventType assign, Locale locale) {
        if (eventTitles == null || !eventTitles.containsKey(assign))
            return null;
        return eventTitles.get(assign).getTranslation(locale);
    }*/

    public boolean isAutoTriggered() {
        if (triggers == null || triggers.isEmpty()) {
            return false;
        }
        return triggers.stream().anyMatch(trigger -> trigger != null && TriggerType.AUTO.equals(trigger.getType()));
    }

    private void compareExistingUserPermissions(String userId, Map<TaskPermission, Boolean> permissions) {
        // TODO: release/8.0.0 check if possible to reduce duplicated code, possible solution is to have abstraction on permissions map
//        permissions.forEach((id, perm) -> {
//            if ((users.containsKey(userId) && !users.get(userId).containsKey(id)) || (users.containsKey(userId) && users.get(userId).containsKey(id) && users.get(userId).get(id))) {
//                users.get(userId).put(id, perm);
//            }
//        });
    }
}