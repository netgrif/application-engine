package com.netgrif.application.engine.event.dispatchers;


import com.netgrif.core.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.core.event.events.action.ActionStartEvent;
import com.netgrif.core.event.events.action.ActionStopEvent;
import org.springframework.context.event.EventListener;

import java.util.Set;

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
