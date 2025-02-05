package com.netgrif.application.engine.history.service.listener;

import com.netgrif.application.engine.event.dispatchers.CaseDispatcher;
import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.core.event.events.workflow.CreateCaseEvent;
import com.netgrif.core.event.events.workflow.DeleteCaseEvent;
import com.netgrif.application.engine.event.listeners.Listener;
import com.netgrif.core.history.domain.caseevents.CreateCaseEventLog;
import com.netgrif.core.history.domain.caseevents.DeleteCaseEventLog;
import com.netgrif.application.engine.history.domain.caseevents.repository.CreateCaseEventLogRepository;
import com.netgrif.application.engine.history.domain.caseevents.repository.DeleteCaseEventLogRepository;
import org.springframework.stereotype.Component;

import java.util.EventObject;
import java.util.Set;

@Component
public class CaseEventListener extends Listener {
    private final CreateCaseEventLogRepository createCaseEventLogRepository;
    private final DeleteCaseEventLogRepository deleteCaseEventLogRepository;

    public CaseEventListener(CaseDispatcher dispatcher,
                             CreateCaseEventLogRepository createCaseEventLogRepository,
                             DeleteCaseEventLogRepository deleteCaseEventLogRepository) {
        this.createCaseEventLogRepository = createCaseEventLogRepository;
        this.deleteCaseEventLogRepository = deleteCaseEventLogRepository;
        this.registerAll(dispatcher,
                Set.of(CreateCaseEvent.class,
                        DeleteCaseEvent.class),
                AbstractDispatcher.DispatchMethod.ASYNC);
    }

    @Override
    public void onAsyncEvent(EventObject event, AbstractDispatcher dispatcher) {
        if (event instanceof CreateCaseEvent) {
            createCaseEventLogRepository.save(CreateCaseEventLog.fromEvent((CreateCaseEvent) event));
        } else if (event instanceof DeleteCaseEvent) {
            deleteCaseEventLogRepository.save(DeleteCaseEventLog.fromEvent((DeleteCaseEvent) event));
        }
    }

    @Override
    public void onEvent(EventObject event, AbstractDispatcher dispatcher) {
        //do nothing
    }
}
