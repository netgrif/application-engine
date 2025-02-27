//package com.netgrif.application.engine.event.events.task;
//
//import com.netgrif.application.engine.auth.domain.IUser;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.petrinet.domain.events.EventType;
//import com.netgrif.core.workflow.domain.eventoutcomes.taskoutcomes.DelegateTaskEventOutcome;
//import lombok.Getter;
//
//public class DelegateTaskEvent extends TaskEvent {
//
//    @Getter
//    private String delegate;
//
//    public DelegateTaskEvent(DelegateTaskEventOutcome eventOutcome, EventPhase eventPhase) {
//        super(eventOutcome, eventPhase);
//    }
//
//    public DelegateTaskEvent(DelegateTaskEventOutcome eventOutcome, EventPhase eventPhase, String delegate) {
//        super(eventOutcome, eventPhase);
//        this.delegate = delegate;
//    }
//
//    public DelegateTaskEvent(DelegateTaskEventOutcome eventOutcome, IUser user, String delegate) {
//        super(eventOutcome, user);
//        this.delegate = delegate;
//    }
//
//    @Override
//    public String getMessage() {
//        return "DelegateTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] delegated";
//    }
//    @Override
//    public EventType getEventType() {
//        return EventType.DELEGATE;
//    }
//}
