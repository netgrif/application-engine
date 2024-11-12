package com.netgrif.application.engine.event.listeners;


import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.dispatchers.common.DispatchMethod;
import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.event.events.EventAction;

import java.util.function.Function;


public abstract class Listener {

    public Listener() {
    }

    public Listener(AbstractDispatcher dispatcher, EventAction eventAction, DispatchMethod method) {
        dispatcher.registerListener(this, eventAction, method);
    }

    /***
     * <p>Abstract method for handling synchronous events. This method will be invoked by Dispatcher if the Listener is registered.
     * See {@link AbstractDispatcher#registerListener(Listener, EventAction, DispatchMethod)}</p>
     * @param event {@link Event} object, final type of the object is determined based on {@link EventAction}
     * @param dispatcher {@link AbstractDispatcher} from which the event was dispatched
     */
    public abstract void onEvent(Event event, AbstractDispatcher dispatcher);

    /***
     * <p>Abstract method for handling asynchronous events. This method will be invoked by Dispatcher if the Listener is registered.
     * See {@link AbstractDispatcher#registerListener(Listener, EventAction, DispatchMethod)}</p>
     * @param event {@link Event} object, final type of the object is determined based on {@link EventAction}
     * @param dispatcher {@link AbstractDispatcher} from which the event was dispatched
     */
    public abstract void onAsyncEvent(Event event, AbstractDispatcher dispatcher);

    public String getName(){
        return this.getClass().getSimpleName();
    }

}
