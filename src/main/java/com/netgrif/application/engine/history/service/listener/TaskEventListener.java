package com.netgrif.application.engine.history.service.listener;

import com.netgrif.application.engine.event.dispatchers.TaskDispatcher;
import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.events.task.*;
import com.netgrif.application.engine.event.events.workflow.CreateCaseEvent;
import com.netgrif.application.engine.event.events.workflow.DeleteCaseEvent;
import com.netgrif.application.engine.event.listeners.Listener;
import com.netgrif.application.engine.history.domain.caseevents.CreateCaseEventLog;
import com.netgrif.application.engine.history.domain.taskevents.AssignTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.CreateTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.DelegateTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.FinishTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.repository.*;
import org.springframework.stereotype.Component;

import java.util.EventObject;
import java.util.Set;

@Component
public class TaskEventListener extends Listener {

    private final AssignTaskEventLogRepository assignTaskEventLogRepository;
    private final CancelTaskEventLogRepository cancelTaskEventLogRepository;
    private final CreateTaskEventLogRepository createTaskEventLogRepository;
    private final DelegateTaskEventLogRepository delegateTaskEventLogRepository;
    private final FinishTaskEventLogRepository finishTaskEventLogRepository;

    public TaskEventListener(TaskDispatcher dispatcher,
                             AssignTaskEventLogRepository assignTaskEventLogRepository,
                             CancelTaskEventLogRepository cancelTaskEventLogRepository,
                             CreateTaskEventLogRepository createTaskEventLogRepository,
                             DelegateTaskEventLogRepository delegateTaskEventLogRepository,
                             FinishTaskEventLogRepository finishTaskEventLogRepository) {
        this.assignTaskEventLogRepository = assignTaskEventLogRepository;
        this.cancelTaskEventLogRepository = cancelTaskEventLogRepository;
        this.createTaskEventLogRepository = createTaskEventLogRepository;
        this.delegateTaskEventLogRepository = delegateTaskEventLogRepository;
        this.finishTaskEventLogRepository = finishTaskEventLogRepository;
        this.registerAll(dispatcher,
                Set.of(AssignTaskEvent.class,
                        CancelTaskEvent.class,
                        CreateTaskEvent.class,
                        DelegateTaskEvent.class,
                        FinishTaskEvent.class),
                AbstractDispatcher.DispatchMethod.ASYNC);
    }


    @Override
    public void onAsyncEvent(EventObject event, AbstractDispatcher dispatcher) {
        if (event instanceof AssignTaskEvent) {
            assignTaskEventLogRepository.save(AssignTaskEventLog.fromEvent((AssignTaskEvent) event));
        } else if (event instanceof CancelTaskEvent) {
//            cancelTaskEventLogRepository.save(CancelTaskEventLog.from((CancelTaskEvent) event));
        } else if (event instanceof CreateTaskEvent) {
            createTaskEventLogRepository.save(CreateTaskEventLog.fromEvent((CreateTaskEvent) event));
        } else if (event instanceof DelegateTaskEvent) {
            delegateTaskEventLogRepository.save(DelegateTaskEventLog.fromEvent((DelegateTaskEvent) event));
        }else if (event instanceof FinishTaskEvent) {
            finishTaskEventLogRepository.save(FinishTaskEventLog.fromEvent((FinishTaskEvent) event));
        }
    }

    @Override
    public void onEvent(EventObject event, AbstractDispatcher dispatcher) {
        // do nothing
    }
}
