package com.netgrif.application.engine.event.dispatchers.common;

import com.netgrif.application.engine.event.events.EventAction;
import com.netgrif.application.engine.event.listeners.Listener;

public record RegisteredListener(Listener listener, EventAction eventAction, DispatchMethod dispatchMethod) {
}
