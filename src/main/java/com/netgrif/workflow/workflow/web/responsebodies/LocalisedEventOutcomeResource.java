package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.workflow.domain.EventOutcome;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;
import java.util.Locale;

public class LocalisedEventOutcomeResource extends Resource<LocalisedEventOutcome> {

    public LocalisedEventOutcomeResource(LocalisedEventOutcome content) {
        super(content, new ArrayList<Link>());
    }

    public static LocalisedEventOutcomeResource successOutcome(EventOutcome outcome, Locale locale, String defaultSuccessMessage) {
        return new LocalisedEventOutcomeResource(LocalisedEventOutcome.successOutcome(outcome, locale, defaultSuccessMessage));
    }

    public static LocalisedEventOutcomeResource errorOutcome(String errorMessage) {
        return new LocalisedEventOutcomeResource(LocalisedEventOutcome.errorOutcome(errorMessage));
    }
}
