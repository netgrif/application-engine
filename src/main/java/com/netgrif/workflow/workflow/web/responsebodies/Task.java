package com.netgrif.workflow.workflow.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.auth.web.responsebodies.User;
import com.netgrif.workflow.elastic.domain.ElasticTask;
import com.netgrif.workflow.petrinet.domain.EventType;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.layout.TaskLayout;
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
public class Task {

    @JsonIgnore
    private ObjectId _id;

    private String caseId;

    private String transitionId;

    private TaskLayout layout;

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

    private String dataFocusPolicy;

    private String finishPolicy;

    private String finishTitle;

    private String cancelTitle;

    private String delegateTitle;

    private String assignTitle;

    public Task(com.netgrif.workflow.workflow.domain.Task task, Locale locale) {
        this._id = task.getObjectId();
        this.caseId = task.getCaseId();
        this.transitionId = task.getTransitionId();
        this.layout = task.getLayout();
        this.title = task.getTitle().getTranslation(locale);
        this.caseColor = task.getCaseColor();
        this.caseTitle = task.getCaseTitle();
        this.priority = task.getPriority();
        this.user = new User(task.getUser(), true);
        this.roles = task.getRoles();
        this.startDate = task.getStartDate();
        this.finishDate = task.getFinishDate();
        this.finishedBy = task.getFinishedBy();
        this.transactionId = task.getTransactionId();
        this.requiredFilled = task.getRequiredFilled();
        this.immediateData = task.getImmediateData();
        this.icon = task.getIcon();
        this.assignPolicy = task.getAssignPolicy().toString();
        this.dataFocusPolicy = task.getDataFocusPolicy().toString();
        this.finishPolicy = task.getFinishPolicy().toString();
        this.finishTitle = task.getTranslatedEventTitle(EventType.FINISH, locale);
        this.assignTitle = task.getTranslatedEventTitle(EventType.ASSIGN, locale);
        this.cancelTitle = task.getTranslatedEventTitle(EventType.CANCEL, locale);
        this.delegateTitle = task.getTranslatedEventTitle(EventType.DELEGATE, locale);
    }

    public Task(ElasticTask entity) {
        _id = new ObjectId(entity.getStringId());
        caseId = entity.getCaseId();
        transitionId = entity.getTransitionId();
        title = entity.getTitle();
        caseTitle = entity.getCaseTitle();
        priority = entity.getPriority();
    }

    public String getStringId() {
        return _id.toString();
    }
}