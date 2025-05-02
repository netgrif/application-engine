package com.netgrif.application.engine.event.dispatchers;


import com.netgrif.application.engine.objects.event.dispatchers.common.AbstractDispatcher;

import com.netgrif.application.engine.objects.event.events.event.ActionStartEvent;
import com.netgrif.application.engine.objects.event.events.event.ActionStopEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ActionDispatcher extends AbstractDispatcher {
    protected ActionDispatcher() {
        super(Set.of(ActionStartEvent.class, ActionStopEvent.class));
    }

    @EventListener
    public void handleActionStartEvent(ActionStartEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncActionStartEvent(ActionStartEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleActionStopEvent(ActionStopEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncActionStopEvent(ActionStopEvent event) {
        dispatchAsync(event);
    }
}
