package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.events.EventAction;
import com.netgrif.application.engine.event.events.petrinet.ProcessDeleteEvent;
import com.netgrif.application.engine.event.events.petrinet.ProcessDeployEvent;
import org.springframework.context.event.EventListener;

import java.util.Set;

public class ProcessDispatcher extends AbstractDispatcher {

    public ProcessDispatcher() {
        super(Set.of(EventAction.PROCESS_DELETE, EventAction.PROCESS_DEPLOY));
    }

    @EventListener
    public void handleProcessDeployEvent(ProcessDeployEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.PROCESS_DEPLOY
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncProcessDeployEvent(ProcessDeployEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.PROCESS_DEPLOY
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleProcessDeleteEvent(ProcessDeleteEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.PROCESS_DELETE
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncProcessDeleteEvent(ProcessDeleteEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.PROCESS_DELETE
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }
}
