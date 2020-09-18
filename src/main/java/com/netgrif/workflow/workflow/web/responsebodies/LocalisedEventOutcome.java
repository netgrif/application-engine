package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.workflow.domain.EventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedEventOutcome extends ChangedFieldContainer {

    private String successMessage;
    private String errorMessage;
    private User assignee;

    public LocalisedEventOutcome() {
        super();
    }

    public static LocalisedEventOutcome successOutcome(EventOutcome outcome, Locale locale, String defaultSuccessMessage) {
        LocalisedEventOutcome result = new LocalisedEventOutcome();
        result.putAll(outcome.getChangedFields());
        result.assignee = outcome.getAssignee();
        if (outcome.getMessage() != null) {
            result.successMessage = outcome.getMessage().getTranslation(locale);
        } else {
            result.successMessage = defaultSuccessMessage;
        }
        return result;
    }

    public static LocalisedEventOutcome errorOutcome(String errorMessage) {
        LocalisedEventOutcome result = new LocalisedEventOutcome();
        result.errorMessage = errorMessage;
        return result;
    }
}
