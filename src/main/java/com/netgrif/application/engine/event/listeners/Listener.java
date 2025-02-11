package com.netgrif.application.engine.event.listeners;


import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.events.EventChain;
import lombok.Getter;

import java.util.EventListener;
import java.util.EventObject;
import java.util.Set;

@Getter
public abstract class Listener implements EventListener {

    private EventChain eventChain;

    private int eventCount = 0;

    public Listener() {
        this.eventChain = new EventChain(Set.of(this.getClass()));
    }
    //todo: register global

    /**
     * Register this listener to dispatcher.
     *
     * @param dispatcher     Dispatcher, to listen Events
     * @param event          Clas of event, which this Listener will be subscribed to
     * @param dispatchMethod synchronous or asynchronous
     * @see AbstractDispatcher#registerListener(Listener, Class, AbstractDispatcher.DispatchMethod)
     */
    public void register(AbstractDispatcher dispatcher, Class<? extends EventObject> event, AbstractDispatcher.DispatchMethod dispatchMethod) {
        dispatcher.registerListener(this, event, dispatchMethod);
    }

    /**
     * Register this listener to dispatcher.
     *
     * @param dispatcher     Dispatcher, to listen Events
     * @param events         Set of Events that the listener will listen to
     * @param dispatchMethod synchronous or asynchronous
     */
    public void registerAll(AbstractDispatcher dispatcher, Set<Class<? extends EventObject>> events, AbstractDispatcher.DispatchMethod dispatchMethod) {
        events.forEach(event -> dispatcher.registerListener(this, event, dispatchMethod));
    }

    /**
     * Unregister this listener to dispatcher.
     *
     * @param dispatcher     Dispatcher, from which listener will be unsubscribed
     * @param event          Clas of event, from which this Listener will be unsubscribed
     * @param dispatchMethod synchronous or asynchronous
     * @see AbstractDispatcher#registerListener(Listener, Class, AbstractDispatcher.DispatchMethod)
     */
    public void unregister(AbstractDispatcher dispatcher, Class<? extends EventObject> event, AbstractDispatcher.DispatchMethod dispatchMethod) {
        dispatcher.unregisterListener(this, event, dispatchMethod);
    }

    /**
     * Unregister this listener to dispatcher.
     *
     * @param dispatcher     Dispatcher, from which listener will be unsubscribed
     * @param events         Set of Events from which this Listener will be unsubscribed
     * @param dispatchMethod synchronous or asynchronous
     * @see AbstractDispatcher#registerListener(Listener, Class, AbstractDispatcher.DispatchMethod)
     */
    public void unregisterAll(AbstractDispatcher dispatcher, Set<Class<? extends EventObject>> events, AbstractDispatcher.DispatchMethod dispatchMethod) {
        events.forEach(event -> dispatcher.unregisterListener(this, event, dispatchMethod));
    }

    /**
     * TODO
     *
     * @param event
     * @param dispatcher
     * @param <E>
     */
    public <E extends EventObject> void onPreEvent(final E event, AbstractDispatcher dispatcher) {
        eventChain.add(event, this.getClass());
        eventCount++;
        onEvent(event, dispatcher);
        if (eventChain.hasChild()) {
            onEventChainFull();
            this.eventChain = eventChain.getChild();
        }
    }

    /**
     * <p>Abstract method for handling synchronous events. This method will be invoked by Dispatcher if the Listener is registered.
     *
     * @param event      {@link EventObject} object, final type of the object is determined based on registered events
     * @param dispatcher {@link AbstractDispatcher} from which the event was dispatched
     * @see AbstractDispatcher#registerListener(Listener, Class, AbstractDispatcher.DispatchMethod)
     */
    public abstract <E extends EventObject> void onEvent(final E event, AbstractDispatcher dispatcher);


    /**
     * TODO
     *
     * @param event
     * @param dispatcher
     * @param <E>
     */
    public <E extends EventObject> void onPreAsyncEvent(final E event, AbstractDispatcher dispatcher) {
        eventChain.add(event, this.getClass());
        eventCount++;
        onAsyncEvent(event, dispatcher);
        if (eventChain.hasChild()) {
            onEventChainFull();
            this.eventChain = eventChain.getChild();
        }
    }

    /**
     * <p>Abstract method for handling asynchronous events. This method will be invoked by Dispatcher if the Listener is registered.
     *
     * @param event      {@link EventObject} object, final type of the object is determined based on registered events
     * @param dispatcher {@link AbstractDispatcher} from which the event was dispatched
     * @see AbstractDispatcher#registerListener(Listener, Class, AbstractDispatcher.DispatchMethod)
     */
    public abstract <E extends EventObject> void onAsyncEvent(final E event, AbstractDispatcher dispatcher);

    public void onEventChainFull() {
        if (eventChain.getLength() == eventCount ) {
            eventChain.wipe();
            eventCount = 0;
        }
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}
