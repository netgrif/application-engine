package com.netgrif.workflow.workflow.domain.eventoutcomes;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.LocalisedEventOutcome;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
public abstract class EventOutcome {

    private I18nString message;

    private List<EventOutcome> outcomes = new ArrayList<>();

    protected EventOutcome() {
    }

    protected EventOutcome(I18nString message) {
        this();
        this.message = message;
    }

    protected EventOutcome(I18nString message, List<EventOutcome> outcomes) {
        this(message);
        this.outcomes = outcomes;
    }

    public void addOutcome(EventOutcome eventOutcome) {
        this.outcomes.add(eventOutcome);
    }

    public void addOutcomes(List<EventOutcome> outcomes){
        this.outcomes.addAll(outcomes);
    }
}