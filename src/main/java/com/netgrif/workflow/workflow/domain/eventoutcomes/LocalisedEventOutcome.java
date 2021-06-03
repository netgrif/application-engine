package com.netgrif.workflow.workflow.domain.eventoutcomes;

import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Data
public class LocalisedEventOutcome {

    private String message;

    private List<LocalisedEventOutcome> outcomes;

    public LocalisedEventOutcome(EventOutcome outcome, Locale locale){
        this.message = outcome.getMessage() == null ? null : outcome.getMessage().getTranslation(locale);
        this.outcomes = outcome.getOutcomes() == null ? null : outcome.getOutcomes().stream()
                .map(eventOutcome -> eventOutcome.transformToLocalisedEventOutcome(locale))
                .collect(Collectors.toList());
    }

    public LocalisedEventOutcome(String message, List<LocalisedEventOutcome> outcomes) {
        this.message = message;
        this.outcomes = outcomes;
    }

    public LocalisedEventOutcome() {
    }

    public void addOutcome(LocalisedEventOutcome outcome) {
        this.outcomes.add(outcome);
    }

    public void addOutcomes(List<LocalisedEventOutcome> outcomes){
        this.outcomes.addAll(outcomes);
    }
}
