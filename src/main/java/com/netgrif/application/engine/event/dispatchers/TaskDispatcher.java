package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.core.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.core.event.events.task.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TaskDispatcher extends AbstractDispatcher {

    public TaskDispatcher() {
        super(Set.of(AssignTaskEvent.class,
                CancelTaskEvent.class,
                CreateTaskEvent.class,
                DelegateTaskEvent.class,
                FinishTaskEvent.class,
                IndexTaskEvent.class
        ));
    }

    @EventListener
    public void handleAssignTaskEvent(AssignTaskEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncAssignTaskEvent(AssignTaskEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleCancelTaskEvent(CancelTaskEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncCancelTaskEvent(CancelTaskEvent event) {
        dispatchAsync(event);
    }


    @EventListener
    public void handleCreateTaskEvent(CreateTaskEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncCreateTaskEvent(CreateTaskEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleDelegateTaskEvent(DelegateTaskEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncDelegateTaskEvent(DelegateTaskEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleFinishTaskEvent(FinishTaskEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncFinishTaskEvent(FinishTaskEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleIndexedTaskEvent(IndexTaskEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncIndexedTaskEvent(IndexTaskEvent event) {
        dispatchAsync(event);
    }

}
