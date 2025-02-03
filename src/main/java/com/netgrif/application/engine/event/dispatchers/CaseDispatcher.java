package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.events.workflow.CreateCaseEvent;
import com.netgrif.application.engine.event.events.workflow.DeleteCaseEvent;
import com.netgrif.application.engine.event.events.workflow.IndexCaseEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CaseDispatcher extends AbstractDispatcher {

    protected CaseDispatcher() {
        super(Set.of(DeleteCaseEvent.class, CreateCaseEvent.class, IndexCaseEvent.class));
    }

    @EventListener
    public void handleCreateCaseEvent(CreateCaseEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncCreateCaseEvent(CreateCaseEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleDeleteCaseEvent(DeleteCaseEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleAsyncDeleteCaseEvent(DeleteCaseEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleIndexCaseEvent(IndexCaseEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncIndexCaseEvent(IndexCaseEvent event) {
        dispatchAsync(event);
    }

}
