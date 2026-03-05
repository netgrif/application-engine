package com.netgrif.application.engine.objects.event.listeners;

import com.netgrif.application.engine.objects.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.objects.event.dispatchers.common.DispatcherRegistry;

import java.util.EventObject;

public abstract class ContextEditingListener<T> extends Listener {

    public ContextEditingListener(DispatcherRegistry dispatcherRegistry) {
        super(dispatcherRegistry);
    }

    public ContextEditingListener() {
        super();
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
