package com.netgrif.application.engine.workflow.domain.eventoutcomes;

import com.netgrif.core.petrinet.domain.I18nString;
import com.netgrif.core.petrinet.domain.dataset.logic.FrontAction;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public abstract class EventOutcome implements Serializable {

    private static final long serialVersionUID = 5228218326436828485L;

//    todo doplnenie referencie na event po implementácii event loggingu

    private I18nString message;

    private List<EventOutcome> outcomes = new ArrayList<>();

    private List<FrontAction> frontActions = new ArrayList<>();

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

    public EventOutcome(I18nString message, List<EventOutcome> outcomes, List<FrontAction> frontActions) {
        this(message, outcomes);
        this.frontActions = frontActions;
    }

    public void addOutcome(EventOutcome eventOutcome) {
        this.outcomes.add(eventOutcome);
    }

    public void addOutcomes(List<EventOutcome> outcomes) {
        this.outcomes.addAll(outcomes);
    }

    public void addFrontAction(FrontAction frontAction) {
        this.frontActions.add(frontAction);
    }
}