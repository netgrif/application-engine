package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base;

import com.netgrif.application.engine.eventoutcomes.LocalisedEventOutcomeFactory;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Data
public abstract class LocalisedEventOutcome {

    private String message;

    private List<LocalisedEventOutcome> outcomes;

    protected LocalisedEventOutcome() {
    }

    protected LocalisedEventOutcome(EventOutcome outcome, Locale locale){
        this.message = outcome.getMessage() == null ? null : outcome.getMessage().getTranslation(locale);
        this.outcomes = outcome.getOutcomes() == null ? null : outcome.getOutcomes().stream()
                .map(eventOutcome -> LocalisedEventOutcomeFactory.from(eventOutcome, locale))
                .collect(Collectors.toList());
    }

    protected LocalisedEventOutcome(String message, List<LocalisedEventOutcome> outcomes) {
        this.message = message;
        this.outcomes = outcomes;
    }

    public void addOutcome(LocalisedEventOutcome outcome) {
        this.outcomes.add(outcome);
    }

    public void addOutcomes(List<LocalisedEventOutcome> outcomes){
        this.outcomes.addAll(outcomes);
    }
}
