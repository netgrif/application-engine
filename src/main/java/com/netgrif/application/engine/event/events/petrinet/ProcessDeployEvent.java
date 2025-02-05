//package com.netgrif.application.engine.event.events.petrinet;
//
//import com.netgrif.core.event.events.Event;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
//import lombok.Getter;
//
//public class ProcessDeployEvent extends ProcessEvent {
//
//    @Getter
//    protected final ImportPetriNetEventOutcome eventOutcome;
//
//    public ProcessDeployEvent(ImportPetriNetEventOutcome eventOutcome, EventPhase eventPhase) {
//        super(eventOutcome, eventPhase);
//        this.eventOutcome = eventOutcome;
//    }
//
//    @Override
//    public String getMessage() {
//        return "ProcessDeployEvent: PetriNet [" + eventOutcome.getNet().getIdentifier() + "] imported";
//    }
//}
