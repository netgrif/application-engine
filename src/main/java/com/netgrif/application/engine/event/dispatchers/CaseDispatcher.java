package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.dispatchers.common.DispatchMethod;
import com.netgrif.application.engine.event.dispatchers.common.RegisteredListener;
import com.netgrif.application.engine.event.events.EventAction;
import com.netgrif.application.engine.event.events.workflow.CreateCaseEvent;
import com.netgrif.application.engine.event.events.workflow.DeleteCaseEvent;
import com.netgrif.application.engine.event.events.workflow.IndexCaseEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CaseDispatcher extends AbstractDispatcher {

    public CaseDispatcher() {
        super(Set.of(EventAction.CASE_CREATE, EventAction.CASE_DELETE, EventAction.CASE_INDEXED));
    }

    @EventListener
    public void handleCreateCaseEvent(CreateCaseEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.CASE_CREATE
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncCreateCaseEvent(CreateCaseEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.CASE_CREATE
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleDeleteCaseEvent(DeleteCaseEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.CASE_DELETE
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncDeleteCaseEvent(DeleteCaseEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.CASE_DELETE
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleIndexCaseEvent(IndexCaseEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.CASE_INDEXED
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncIndexCaseEvent(IndexCaseEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.CASE_INDEXED
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

}
