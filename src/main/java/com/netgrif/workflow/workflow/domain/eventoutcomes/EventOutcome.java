package com.netgrif.workflow.workflow.domain.eventoutcomes;

import com.netgrif.workflow.petrinet.domain.I18nString;
import lombok.Data;

import java.util.List;

@Data
public abstract class EventOutcome {

    private I18nString message;

    private List<EventOutcome> outcomes;

    public EventOutcome(I18nString message, List<EventOutcome> outcomes) {
        this.message = message;
        this.outcomes = outcomes;
    }

    public EventOutcome(I18nString message) {
        this.message = message;
    }

    public EventOutcome() {
    }

    public void addOutcome(EventOutcome eventOutcome) {
        this.outcomes.add(eventOutcome);
    }

    public void addOutcomes(List<EventOutcome> outcomes){
        this.outcomes.addAll(outcomes);
    }
}