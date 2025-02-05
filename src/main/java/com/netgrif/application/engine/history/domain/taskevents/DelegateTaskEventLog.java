//package com.netgrif.application.engine.history.domain.taskevents;
//
//import com.netgrif.core.auth.domain.IUser;
//import com.netgrif.core.event.events.task.CancelTaskEvent;
//import com.netgrif.core.event.events.task.DelegateTaskEvent;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.workflow.domain.Case;
//import com.netgrif.core.workflow.domain.Task;
//import lombok.EqualsAndHashCode;
//import lombok.Getter;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//@EqualsAndHashCode(callSuper = true)
//public class DelegateTaskEventLog extends TaskEventLog {
//
//    @Getter
//    private String delegator;
//
//    @Getter
//    private String delegate;
//
//    public DelegateTaskEventLog() {
//        super();
//    }
//
//    public DelegateTaskEventLog(Task task, Case useCase, EventPhase eventPhase, IUser delegator, String delegate) {
//        super(task, useCase, eventPhase, delegator.getStringId(), delegator.isImpersonating() ? delegator.getImpersonated().getStringId() : null);
//        this.delegator = getUserId();
//        this.delegate = delegate;
//    }
//
//
//    public static DelegateTaskEventLog fromEvent(DelegateTaskEvent event) {
//        return new DelegateTaskEventLog(event.getTaskEventOutcome().getTask(), event.getTaskEventOutcome().getCase(), event.getEventPhase(), event.getUser(), event.getDelegate());
//    }
//}
