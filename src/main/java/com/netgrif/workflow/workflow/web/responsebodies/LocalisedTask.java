package com.netgrif.workflow.workflow.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Localised task data object
 */
@Data
@JsonRootName("task")
public class LocalisedTask {

    @JsonIgnore
    private ObjectId _id;

    private String caseId;

    private String transitionId;

    private String title;

    private String caseColor;

    private String caseTitle;

    private Integer priority;

    private User user;

    private Map<String, Map<String, Boolean>> roles;

    private LocalDateTime startDate;

    private LocalDateTime finishDate;

    private Long finishedBy;

    private String transactionId;

    private Boolean requiredFilled;

    private List<Field> immediateData;

    private String icon;

    private String assignPolicy;

    public LocalisedTask(com.netgrif.workflow.workflow.domain.Task task, Locale locale) {
        this._id = task.getObjectId();
        this.caseId = task.getCaseId();
        this.transitionId = task.getTransitionId();
        this.title = task.getTitle().getTranslation(locale);
        this.caseColor = task.getCaseColor();
        this.caseTitle = task.getCaseTitle();
        this.priority = task.getPriority();
        this.user = task.getUser();
        this.roles = task.getRoles();
        this.startDate = task.getStartDate();
        this.finishDate = task.getFinishDate();
        this.finishedBy = task.getFinishedBy();
        this.transactionId = task.getTransactionId();
        this.requiredFilled = task.getRequiredFilled();
        this.immediateData = task.getImmediateData();
        this.icon = task.getIcon();
        this.assignPolicy = task.getAssignPolicy().toString();
    }

    public String getStringId() {
        return _id.toString();
    }
}