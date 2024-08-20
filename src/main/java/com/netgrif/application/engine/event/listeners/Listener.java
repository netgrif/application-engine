package com.netgrif.application.engine.event.listeners;


import com.netgrif.application.engine.event.dispatchers.AbstractDispatcher;
import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.event.events.EventAction;



public abstract class Listener {

    public Listener() {
    }

    public Listener(AbstractDispatcher dispatcher, EventAction eventAction, AbstractDispatcher.DispatchMethod method) {
        dispatcher.registerListener(this, eventAction, method);
    }

    public abstract void onEvent(Event event, AbstractDispatcher dispatcher);

    public abstract void onAsyncEvent(Event event, AbstractDispatcher dispatcher);

    public String getName(){
        return this.getClass().getSimpleName();
    }


}
