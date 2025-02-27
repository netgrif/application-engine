//package com.netgrif.application.engine.event.events.task;
//
//import com.netgrif.application.engine.auth.domain.IUser;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.petrinet.domain.events.EventType;
//import com.netgrif.core.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
//import com.netgrif.core.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
//
//public class CancelTaskEvent extends TaskEvent {
//
//    public CancelTaskEvent(CancelTaskEventOutcome eventOutcome, EventPhase eventPhase) {
//        super(eventOutcome, eventPhase);
//    }
//
//    public CancelTaskEvent(CancelTaskEventOutcome eventOutcome, IUser user) {
//        super(eventOutcome, user);
//    }
//
//    @Override
//    public String getMessage() {
//        return "CancelTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] cancelled";
//    }
//    @Override
//    public EventType getEventType() {
//        return EventType.CANCEL;
//    }
//}
