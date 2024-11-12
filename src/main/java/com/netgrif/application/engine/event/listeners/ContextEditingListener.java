package com.netgrif.application.engine.event.listeners;

import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.dispatchers.common.DispatchMethod;
import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.event.events.EventAction;

public abstract class ContextEditingListener<T> extends Listener {

    public ContextEditingListener() {
    }

    public ContextEditingListener(AbstractDispatcher dispatcher, EventAction eventAction, DispatchMethod method) {
        super(dispatcher, eventAction, method);
    }

    /***
     * <p>Abstract method for handling synchronous events with return. This method will be invoked by Dispatcher if the Listener is registered.
     * See {@link AbstractDispatcher#registerListener(Listener, EventAction, DispatchMethod)}</p>
     * @param event {@link Event} object, final type of the object is determined based on {@link EventAction}
     * @param dispatcher {@link AbstractDispatcher} from which the event was dispatched
     */
    public abstract T onContextEditingEvent(Event event, AbstractDispatcher dispatcher);
}
