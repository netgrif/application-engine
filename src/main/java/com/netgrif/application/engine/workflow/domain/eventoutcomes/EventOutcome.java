package com.netgrif.application.engine.workflow.domain.eventoutcomes;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class EventOutcome {

//    todo doplnenie referencie na event po implementácii event loggingu

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