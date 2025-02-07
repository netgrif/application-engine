//package com.netgrif.application.engine.history.domain.caseevents;
//
//import com.netgrif.core.event.events.workflow.CreateCaseEvent;
//import com.netgrif.core.event.events.workflow.DeleteCaseEvent;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.workflow.domain.Case;
//import lombok.EqualsAndHashCode;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.io.Serial;
//
//
//@EqualsAndHashCode(callSuper = true)
//public class DeleteCaseEventLog extends CaseEventLog {
//
//    @Serial
//    private static final long serialVersionUID = 2263046649744238557L;
//
//    public DeleteCaseEventLog() {
//        super();
//    }
//
//    public DeleteCaseEventLog(Case useCase, EventPhase eventPhase) {
//        super(useCase, eventPhase);
//    }
//
//    public static DeleteCaseEventLog fromEvent(DeleteCaseEvent event) {
//        return new DeleteCaseEventLog(event.getCaseEventOutcome().getCase(), event.getEventPhase());
//    }
//}
