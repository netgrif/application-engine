//package com.netgrif.application.engine.event.events.task;
//
//import com.netgrif.application.engine.auth.domain.IUser;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.petrinet.domain.events.EventType;
//import com.netgrif.core.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
//
//public class CreateTaskEvent extends TaskEvent {
//
//    public CreateTaskEvent(TaskEventOutcome eventOutcome, EventPhase eventPhase) {
//        super(eventOutcome, eventPhase);
//    }
//
//    public CreateTaskEvent(TaskEventOutcome eventOutcome, EventPhase eventPhase, IUser user) {
//        super(eventOutcome, eventPhase, user);
//    }
//
//    @Override
//    public String getMessage() {
//        return "CreateTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] created";
//    }
//
//    @Override
//    public EventType getEventType() {
//        return null;
//    }
//}
