package com.netgrif.application.engine.event.listeners;

import com.netgrif.application.engine.event.dispatchers.AbstractDispatcher;
import com.netgrif.application.engine.event.dispatchers.CaseDispatcher;
import com.netgrif.application.engine.event.dispatchers.UserDispatcher;
import com.netgrif.application.engine.event.events.EventAction;
import com.netgrif.application.engine.event.dispatchers.TaskDispatcher;
import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.event.events.task.AssignTaskEvent;
import com.netgrif.application.engine.event.events.workflow.IndexCaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleListener extends Listener {

    public SimpleListener(TaskDispatcher taskDispatcher, UserDispatcher userDispatcher, CaseDispatcher caseDispatcher) {
        super(taskDispatcher, EventAction.TASK_ASSIGN, AbstractDispatcher.DispatchMethod.SYNC);
        taskDispatcher.registerListener(this, EventAction.TASK_ASSIGN, AbstractDispatcher.DispatchMethod.ASYNC);
//        taskDispatcher.registerListener(this, EventAction.TASK_ASSIGN, AbstractDispatcher.DispatchMethod.SYNC);
//        userDispatcher.registerListener(this, EventAction.TASK_ASSIGN, AbstractDispatcher.DispatchMethod.SYNC);
        userDispatcher.registerListener(this, EventAction.USER_LOGOUT, AbstractDispatcher.DispatchMethod.SYNC);
        caseDispatcher.registerListener(this, EventAction.CASE_INDEXED, AbstractDispatcher.DispatchMethod.ASYNC);
    }

    @Override
    public void onEvent(Event event, AbstractDispatcher dispatcher) {
        log.info(event.getMessage());
        dispatcher.unregisterListener(this, EventAction.TASK_ASSIGN, AbstractDispatcher.DispatchMethod.SYNC);
    }

    @Override
    public void onAsyncEvent(Event event, AbstractDispatcher dispatcher) {
        if (event instanceof AssignTaskEvent) {
            log.info("task");
        } else if (event instanceof IndexCaseEvent) {
            log.info("case");
        }
        log.info(event.getMessage());
    }

    @Override
    public String getName(){
        return "jozo";
    }
}
