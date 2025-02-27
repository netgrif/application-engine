//package com.netgrif.application.engine.event.events.task;
//
//import com.netgrif.application.engine.auth.domain.IUser;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.petrinet.domain.events.EventType;
//import com.netgrif.core.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
//
//public class FinishTaskEvent extends TaskEvent {
//
//    public FinishTaskEvent(FinishTaskEventOutcome eventOutcome, EventPhase eventPhase) {
//        super(eventOutcome, eventPhase);
//    }
//
//    public FinishTaskEvent(FinishTaskEventOutcome eventOutcome, IUser user) {
//        super(eventOutcome, user);
//    }
//
//    @Override
//    public String getMessage() {
//        return "FinishTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] finished";
//    }
//
//    @Override
//    public EventType getEventType() {
//        return EventType.FINISH;
//    }
//}
