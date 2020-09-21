package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.workflow.domain.EventOutcome;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Locale;

@Data
public class LocalisedEventOutcome extends ChangedFieldContainer {

    private String success;
    private String error;
    private User assignee;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;

    public LocalisedEventOutcome() {
        super();
    }

    public static LocalisedEventOutcome successOutcome(EventOutcome outcome, Locale locale, String defaultSuccessMessage) {
        LocalisedEventOutcome result = new LocalisedEventOutcome();
        result.putAll(outcome.getChangedFields());
        result.assignee = outcome.getAssignee();
        result.startDate = outcome.getStartDate();
        result.finishDate = outcome.getFinishDate();
        if (outcome.getMessage() != null) {
            result.success = outcome.getMessage().getTranslation(locale);
        } else {
            result.success = defaultSuccessMessage;
        }
        return result;
    }

    public static LocalisedEventOutcome errorOutcome(String errorMessage) {
        LocalisedEventOutcome result = new LocalisedEventOutcome();
        result.error = errorMessage;
        return result;
    }
}
