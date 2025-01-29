package com.netgrif.application.engine.event.listeners;

import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;

import java.util.EventObject;

public abstract class ContextEditingListener<T> extends Listener {

    public ContextEditingListener() {
    }

    /**
     * <p>Abstract method for handling synchronous events with return. This method will be invoked by Dispatcher if the Listener is registered.
     *
     * @param event      {@link EventObject} object, final type of the object is determined based on event class
     * @param dispatcher {@link AbstractDispatcher} from which the event was dispatched
     * @see AbstractDispatcher#registerListener(Listener, Class, AbstractDispatcher.DispatchMethod)
     */
    public abstract T onContextEditingEvent(EventObject event, AbstractDispatcher dispatcher);
}
