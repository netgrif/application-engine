package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.events.EventChain;
import com.netgrif.application.engine.event.events.petrinet.ProcessDeleteEvent;
import com.netgrif.application.engine.event.events.petrinet.ProcessDeployEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ProcessDispatcher extends AbstractDispatcher {

    public ProcessDispatcher(@Qualifier("GlobalEventChain") EventChain globalEventChain) {
        super(Set.of(ProcessDeployEvent.class, ProcessDeleteEvent.class), globalEventChain);
    }

    @EventListener
    public void handleProcessDeployEvent(ProcessDeployEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncProcessDeployEvent(ProcessDeployEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleProcessDeleteEvent(ProcessDeleteEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncProcessDeleteEvent(ProcessDeleteEvent event) {
        dispatchAsync(event);
    }
}
