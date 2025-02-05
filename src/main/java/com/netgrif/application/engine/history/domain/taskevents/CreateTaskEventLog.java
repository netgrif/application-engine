//package com.netgrif.application.engine.history.domain.taskevents;
//
//import com.netgrif.application.engine.auth.domain.IUser;
//import com.netgrif.application.engine.event.events.task.CreateTaskEvent;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.application.engine.workflow.domain.Case;
//import com.netgrif.application.engine.workflow.domain.Task;
//
//public class CreateTaskEventLog extends TaskEventLog {
//
//    public CreateTaskEventLog() {
//        super();
//    }
//
//    public CreateTaskEventLog(Task task, Case useCase, EventPhase eventPhase, IUser user) {
//        super(task, useCase, eventPhase, user.getStringId(), user.isImpersonating() ? user.getImpersonated().getStringId() : null);
//    }
//
//    public static CreateTaskEventLog fromEvent(CreateTaskEvent event) {
//        return new CreateTaskEventLog(event.getTaskEventOutcome().getTask(), event.getTaskEventOutcome().getCase(), event.getEventPhase(), event.getUser());
//    }
//}
