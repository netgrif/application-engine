package com.netgrif.application.engine.event.dispatchers.common;

import com.netgrif.application.engine.event.listeners.Listener;

import java.util.EventObject;

/***
 * todo
 * @param listener
 * @param eventClass
 * @param dispatchMethod
 */
public record RegisteredListener(Listener listener, Class<? extends EventObject> eventClass, AbstractDispatcher.DispatchMethod dispatchMethod) {

    public static RegisteredListener fromClassInstance(Listener listener, Class<? extends EventObject> eventClass, AbstractDispatcher.DispatchMethod dispatchMethod) {
        return new RegisteredListener(listener, eventClass, dispatchMethod);
    }

    public boolean contains(Listener listener) {
        return this.listener.equals(listener);
    }

    public boolean contains(EventObject eventClass) {
        return this.eventClass.getName().equals(eventClass.getClass().getName());
    }
    
    public boolean contains(AbstractDispatcher.DispatchMethod dispatchMethod) {
        return this.dispatchMethod.equals(dispatchMethod);
    }

}
