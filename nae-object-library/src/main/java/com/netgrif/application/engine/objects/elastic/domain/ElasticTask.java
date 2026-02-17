package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ElasticTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 8399390623172906801L;


    private String id;

    private String processId;

    private String processIdentifier;

    private String caseId;

    private String taskId;

    private String transitionId;

    private I18nString title;

    private String titleSortable;

    private String caseColor;

    private String caseTitle;

    private String caseTitleSortable;

    private int priority;

    private String userId;

    private String userRealmId;

    private String userFullName;
    
    private String username;

    private LocalDateTime startDate;

    private String transactionId;

    private Map<String, Map<String, Boolean>> roles;

    private Map<String, Map<String, Boolean>> actorRefs;

    private Map<String, Map<String, Boolean>> actors;

    private Set<String> viewActorRefs;

    private Set<String> viewRoles;

    private Set<String> negativeViewRoles;

    private Set<String> viewActors;

    private Set<String> negativeViewActors;

    private String icon;

    private String assignPolicy;

    private String dataFocusPolicy;

    private String finishPolicy;

    private Map<String, String> tags;

    public ElasticTask(Task task) {
        this.id = task.getStringId();
        this.processId = task.getProcessId();
        this.processIdentifier = task.getProcessIdentifier();
        this.taskId = task.getStringId();
        this.caseId = task.getCaseId();
        this.transitionId = task.getTransitionId();
        this.title = task.getTitle();
        this.titleSortable = title.getDefaultValue();
        this.caseTitle = task.getCaseTitle();
        this.caseTitleSortable = this.caseTitle;
        if (task.getPriority() != null)
            this.priority = task.getPriority();
        if (task.getAssignee() != null) {
            this.userId = task.getAssignee().getId();
            this.userRealmId = task.getAssignee().getRealmId();
            this.username = task.getAssignee().getUsername();
            this.userFullName = task.getAssignee().getFullName();
        }
        this.startDate = task.getStartDate();
        this.roles = task.getRoles();
        this.actorRefs = task.getActorRefs();
        this.actors = task.getActors();
        this.viewRoles = new HashSet<>(task.getViewRoles());
        this.viewActorRefs = new HashSet<>(task.getViewActorRefs());
        this.negativeViewRoles = new HashSet<>(task.getNegativeViewRoles());
        this.viewActors = new HashSet<>(task.getViewActors());
        this.negativeViewActors = new HashSet<>(task.getNegativeViewActors());
        this.assignPolicy = task.getAssignPolicy().toString();
        this.dataFocusPolicy = task.getDataFocusPolicy().toString();
        this.finishPolicy = task.getFinishPolicy().toString();
        this.tags = new HashMap<>(task.getTags());
    }

    public void update(ElasticTask task) {
        this.title = task.getTitle();
        this.titleSortable = title.getDefaultValue();
        this.caseTitle = task.getCaseTitle();
        this.caseTitleSortable = this.caseTitle;
        this.priority = task.getPriority();
        this.userId = task.getUserId();
        this.userRealmId = task.getUserRealmId();
        this.userFullName = task.getUserFullName();
        this.username = task.getUsername();
        this.startDate = task.getStartDate();
        this.roles = task.getRoles();
        this.viewRoles = task.getViewRoles();
        this.viewActorRefs = task.getViewActorRefs();
        this.negativeViewRoles = task.getNegativeViewRoles();
        this.viewActors = task.getViewActors();
        this.negativeViewActors = task.getNegativeViewActors();
        this.tags = task.getTags();
    }
}
