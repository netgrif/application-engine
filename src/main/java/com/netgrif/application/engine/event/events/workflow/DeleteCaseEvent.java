//package com.netgrif.application.engine.event.events.workflow;
//
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
//
//public class DeleteCaseEvent extends CaseEvent {
//
//    public DeleteCaseEvent(DeleteCaseEventOutcome caseEventOutcome, EventPhase eventPhase) {
//        super(caseEventOutcome, eventPhase);
//    }
//
//    @Override
//    public String getMessage() {
//        return "DeleteCaseEvent: Case [" + caseEventOutcome.getCase().getStringId() + "] deleted";
//    }
//}
