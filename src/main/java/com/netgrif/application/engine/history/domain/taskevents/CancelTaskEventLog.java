//package com.netgrif.application.engine.history.domain.taskevents;
//
//import com.netgrif.core.auth.domain.IUser;
//import com.netgrif.core.event.events.task.CancelTaskEvent;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.workflow.domain.Case;
//import com.netgrif.core.workflow.domain.Task;
//import lombok.EqualsAndHashCode;
//
//@EqualsAndHashCode(callSuper = true)
//public class CancelTaskEventLog extends TaskEventLog {
//
//    public CancelTaskEventLog() {
//        super();
//    }
//
//    public CancelTaskEventLog(Task task, Case useCase, EventPhase eventPhase, IUser user) {
//        super(task, useCase, eventPhase, user.getStringId(), user.isImpersonating() ? user.getImpersonated().getStringId() : null);
//    }
//
//    public static CancelTaskEventLog fromEvent(CancelTaskEvent event) {
//        return new CancelTaskEventLog(event.getTaskEventOutcome().getTask(), event.getTaskEventOutcome().getCase(), event.getEventPhase(), event.getUser());
//    }
//}
