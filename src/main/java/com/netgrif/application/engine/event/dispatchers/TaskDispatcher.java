package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.dispatchers.common.DispatchMethod;
import com.netgrif.application.engine.event.dispatchers.common.RegisteredListener;
import com.netgrif.application.engine.event.events.EventAction;
import com.netgrif.application.engine.event.events.task.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TaskDispatcher extends AbstractDispatcher {

    public TaskDispatcher() {
        super(Set.of(EventAction.TASK_ASSIGN,
                EventAction.TASK_DELEGATE,
                EventAction.TASK_CREATE,
                EventAction.TASK_FINISH,
                EventAction.TASK_CANCEL,
                EventAction.TASK_INDEXED
        ));
    }

    @EventListener
    public void handleAssignTaskEvent(AssignTaskEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_ASSIGN
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncAssignTaskEvent(AssignTaskEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_ASSIGN
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleDelegateTaskEvent(DelegateTaskEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_DELEGATE
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncDelegateTaskEvent(DelegateTaskEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_DELEGATE
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleFinishTaskEvent(FinishTaskEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_FINISH
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncFinishTaskEvent(FinishTaskEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_FINISH
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleCancelTaskEvent(CancelTaskEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_CANCEL
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncCancelTaskEvent(CancelTaskEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_CANCEL
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleIndexedTaskEvent(IndexTaskEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_INDEXED
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncIndexedTaskEvent(IndexTaskEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_INDEXED
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleCreateTaskEvent(CreateTaskEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_CREATE
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncCreateTaskEvent(CreateTaskEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.TASK_CREATE
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

}
