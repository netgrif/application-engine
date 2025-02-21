package com.netgrif.application.engine.event.dispatchers.common;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.event.listeners.ContextEditingListener;
import com.netgrif.application.engine.event.listeners.Listener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;


@Slf4j
public abstract class AbstractDispatcher {

    //TODO: Configure custom executor
    //TODO: Register event

    private final Set<RegisteredListener> registeredListeners;
    @Getter
    private final Set<Class<? extends EventObject>> allowedEvents;

    private final Executor DEFAULT_EXECUTOR = Executors.newWorkStealingPool();

    protected AbstractDispatcher(Set<Class<? extends EventObject>> allowedEvents) {
        this.allowedEvents = allowedEvents;
        this.registeredListeners = new HashSet<>();
    }

    public enum DispatchMethod {
        SYNC,
        ASYNC
    }

    /**
     * <p>Registration of a new {@link Listener}. The Listener will listen for events based on the value
     * of event class and {@link DispatchMethod}. Throws an {@link IllegalArgumentException}
     * if the listener is already registered or if the dispatcher has no provided class in the allowed events.</p>
     *
     * @param listener       {@link Listener} that will listen to {@link Event} with the onEvent/onAsyncEvent method
     * @param eventClass     Class or subclass of EventObject that the listener is subscribed to
     * @param dispatchMethod {@link DispatchMethod} The method by which the dispatcher will send the Event
     * @param <E>            Type of event, must be subclass of {@link EventObject}
     */
    public <E extends EventObject> void registerListener(Listener listener, Class<E> eventClass, DispatchMethod dispatchMethod) {
        RegisteredListener registeredListener = RegisteredListener.fromClassInstance(listener, eventClass, dispatchMethod);
        if (!isRegistrationAllowed(registeredListener)) {
            throw new IllegalArgumentException(
                    "Cannot register Listener " + listener.getName()
                            + " for Dispatcher " + this.getName()
                            + ": Event " + eventClass.getName()
                            + " is not supported in Dispatcher: " + this.getName()
                            + "\n Allowed Events: " + this.allowedEvents
            );
        } else if (isListenerRegistered(registeredListener)) {
            throw new IllegalArgumentException(
                    "Cannot register Listener " + listener.getName()
                            + " for Dispatcher " + this.getName()
                            + " Listener " + listener.getName()
                            + " is already registered in this Dispatcher");
        } else {
            registeredListeners.add(registeredListener);
        }

    }

    /**
     * <p>Unregister already registered {@link Listener}. The Listener
     * will be unregister only for event based on the value of event class and {@link DispatchMethod}.
     * Throws an {@link IllegalArgumentException} if the listener is not registered.</p>
     *
     * @param listener       {@link Listener} that listen to {@link Event} with the onEvent/onAsyncEvent method
     * @param eventClass     Class or subclass of EventObject that the listener is subscribed to
     * @param dispatchMethod {@link DispatchMethod} The method by which the dispatcher is sending the Event
     * @param <E>            Type of event, must be subclass of {@link EventObject}
     */
    public <E extends EventObject> void unregisterListener(Listener listener, Class<E> eventClass, DispatchMethod dispatchMethod) {
        RegisteredListener registeredListener = RegisteredListener.fromClassInstance(listener, eventClass, dispatchMethod);
        if (!isListenerRegistered(registeredListener)) {
            throw new IllegalArgumentException(
                    "Cannot unregister Listener " + listener.getName()
                            + " for Dispatcher " + this.getName()
                            + " Listener " + listener.getName()
                            + " is not registered in this Dispatcher");
        }
        registeredListeners.remove(registeredListener);
    }

    /**
     * <p>Check if {@link Listener} is registered to Dispatcher.</p>
     *
     * @param listener       {@link Listener} that listen to {@link Event} with the onEvent/onAsyncEvent method
     * @param eventClass     Class or subclass of EventObject that the listener is subscribed to
     * @param dispatchMethod {@link DispatchMethod} The method by which the dispatcher is sending the Event
     * @param <E>            Type of event, must be subclass of {@link EventObject}
     * @return true if dispatcher holds record of {@link Listener} to event class with {@link DispatchMethod} else false
     */
    public <E extends EventObject> boolean isListenerRegistered(Listener listener, Class<E> eventClass, DispatchMethod dispatchMethod) {
        return isListenerRegistered(RegisteredListener.fromClassInstance(listener, eventClass, dispatchMethod));
    }

    /**
     * <p>Check if {@link Listener} is registered to Dispatcher.</p>
     *
     * @param registeredListener {@link RegisteredListener} that holds listening content
     * @return true if dispatcher holds record of {@link RegisteredListener} else false
     */
    public boolean isListenerRegistered(RegisteredListener registeredListener) {
        return registeredListeners.stream().anyMatch(l -> l.equals(registeredListener));
    }

    /**
     * <p>Send event object to registered {@link Listener}. This function sends events asynchronously,
     * but wait until all listeners have finished executing the onEvent method. This is wrapper method,
     * where decision function foo as input is omitted. Decision is made by lambda function,
     * which checks if registerListener contains provided event with sync dispatching method </p>
     *
     * @param event Event that the listener is subscribed to
     * @param <E>   Type of event, must be child of {@link EventObject}
     * @see AbstractDispatcher#dispatch(EventObject, Function, Executor)
     * @see RegisteredListener
     */
    protected <E extends EventObject> void dispatch(E event) {
        dispatch(event, (RegisteredListener registeredListener) ->
                registeredListener.contains(event)
                        && registeredListener.contains(DispatchMethod.SYNC), DEFAULT_EXECUTOR);
    }

    /**
     * <p>Send event object to registered {@link Listener}.This function dispatches events asynchronously
     * and does not wait for the execution of the onAsyncEvent method to complete. This is wrapper method,
     * where decision function foo as input is omitted. Decision is made by lambda function,
     * which checks if registerListener contains provided event with async dispatching method </p>
     *
     * @param event Event that the listener is subscribed to
     * @param <E>   Type of event, must be child of {@link EventObject}
     * @see Listener#onAsyncEvent
     * @see RegisteredListener
     */
    protected <E extends EventObject> void dispatchAsync(E event) {
        dispatchAsync(event, (RegisteredListener registeredListener) ->
                registeredListener.contains(event)
                        && registeredListener.contains(DispatchMethod.ASYNC), DEFAULT_EXECUTOR);
    }

    /**
     * <p>Send event object to registered {@link Listener}. This function sends events asynchronously,
     * but wait until all listeners have finished executing the onEvent method.
     * See {@link Listener#onEvent}.</p>
     *
     * @param event    Event that the listener is subscribed to
     * @param foo      Function to decide to which listener the event should be sent
     * @param executor Executor to use for asynchronous execution
     * @param <E>      Type of event, must be child of {@link EventObject}
     */
    @SuppressWarnings("unchecked")
    protected <E extends EventObject> void dispatch(E event, Function<RegisteredListener, Boolean> foo, Executor executor) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<RegisteredListener> simpleListeners = registeredListeners.stream().filter(l -> !(l.listener() instanceof ContextEditingListener<?>)).toList();
        List<RegisteredListener> contextEditingListeners = registeredListeners.stream().filter(l -> l.listener() instanceof ContextEditingListener<?>).toList();
        for (RegisteredListener registeredListener : simpleListeners) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                if (foo.apply(registeredListener)) {
                    log.trace("Sending event {} synchronously", event.getClass().getSimpleName());
                    registeredListener.listener().onEvent(event, this);
                }
            }, executor);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        E updatedEvent = event;
        for (RegisteredListener registeredListener : contextEditingListeners) {
            if (foo.apply(registeredListener)) {
                updatedEvent = ((ContextEditingListener<E>) registeredListener.listener()).onContextEditingEvent(updatedEvent, this);
            }
        }
    }

    /**
     * <p>Send event object to registered {@link Listener}.This function dispatches events asynchronously
     * and does not wait for the execution of the onAsyncEvent method to complete.
     * See {@link Listener#onAsyncEvent}.</p>
     *
     * @param event    Event that the listener is subscribed to
     * @param foo      Function to decide to which listener the event should be sent
     * @param executor Executor to use for asynchronous execution
     * @param <E>      Type of event, must be child of {@link EventObject}
     */
    protected <E extends EventObject> void dispatchAsync(E event, Function<RegisteredListener, Boolean> foo, Executor executor) {
        for (RegisteredListener registeredListener : registeredListeners) {
            CompletableFuture.runAsync(() -> {
                if (foo.apply(registeredListener)) {
                    log.trace("Sending event {} asynchronously", event.getClass().getSimpleName());
                    registeredListener.listener().onAsyncEvent(event, this);
                }
            }, executor);
        }
    }

    protected String getName() {
        return this.getClass().getSimpleName();
    }

    private boolean isRegistrationAllowed(RegisteredListener registeredListener) {
        return allowedEvents.stream().anyMatch(it -> it.equals(registeredListener.eventClass()));
    }

}
