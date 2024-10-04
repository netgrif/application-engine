package com.netgrif.application.engine.event.dispatchers.common;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.event.events.EventAction;
import com.netgrif.application.engine.event.events.task.TaskEvent;
import com.netgrif.application.engine.event.listeners.ContextEditingListener;
import com.netgrif.application.engine.event.listeners.Listener;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
public abstract class AbstractDispatcher {

    //TODO: Configure custom executor

    private final Set<RegisteredListener> registeredListeners;
    @Getter
    private final Set<EventAction> allowedActions;

    protected AbstractDispatcher(Set<EventAction> allowedActions) {
        this.allowedActions = allowedActions;
        this.registeredListeners = new HashSet<>();
    }

    /***
     * <p>Registration of a new {@link Listener}. The Listener will listen for events based on the value
     * of {@link EventAction} and {@link DispatchMethod}. Throws an {@link IllegalArgumentException}
     * if the listener is already registered or if the dispatcher has no {@link EventAction} value in the allowed events.</p>
     * @param listener {@link Listener} that will listen to {@link Event} with the onEvent/onAsyncEvent method
     * @param eventAction {@link EventAction} that the listener will subscribe to
     * @param dispatchMethod {@link DispatchMethod} The method by which the dispatcher will send the Event
     */
    public void registerListener(Listener listener, EventAction eventAction, DispatchMethod dispatchMethod) {
        RegisteredListener registeredListener = new RegisteredListener(listener, eventAction, dispatchMethod);
        if (!isRegistrationAllowed(registeredListener)) {
            throw new IllegalArgumentException(
                    "Cannot register Listener " + listener.getName()
                            + " for Dispatcher " + this.getName()
                            + ": Event " + eventAction
                            + " is not supported in Dispatcher: " + this.getName()
                            + "\n Allowed Events: " + this.allowedActions
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

    /***
     * <p>Unregister already registered {@link Listener}. The Listener
     * will be unregister only for event based on the value of {@link EventAction} and {@link DispatchMethod}.
     * Throws an {@link IllegalArgumentException} if the listener is not registered.</p>
     * @param listener {@link Listener} that listen to {@link Event} with the onEvent/onAsyncEvent method
     * @param eventAction {@link EventAction} that the listener is subscribed to
     * @param dispatchMethod {@link DispatchMethod} The method by which the dispatcher is sending the Event
     */
    public void unregisterListener(Listener listener, EventAction eventAction, DispatchMethod dispatchMethod) {
        RegisteredListener registeredListener = new RegisteredListener(listener, eventAction, dispatchMethod);
        if (!isListenerRegistered(registeredListener)) {
            throw new IllegalArgumentException(
                    "Cannot unregister Listener " + listener.getName()
                            + " for Dispatcher " + this.getName()
                            + " Listener " + listener.getName()
                            + " is not registered in this Dispatcher");
        }
        registeredListeners.remove(registeredListener);
    }

    /***
     * <p>Check if {@link Listener} is registered to Dispatcher.</p>
     * @param listener {@link Listener} that listen to {@link Event} with the onEvent/onAsyncEvent method
     * @param eventAction {@link EventAction} that the listener is subscribed to
     * @param dispatchMethod {@link DispatchMethod} The method by which the dispatcher is sending the Event
     * @return true if dispatcher holds record of {@link Listener} to {@link EventAction} with {@link DispatchMethod} else false
     */
    public boolean isListenerRegistered(Listener listener, EventAction eventAction, DispatchMethod dispatchMethod) {
        return isListenerRegistered(new RegisteredListener(listener, eventAction, dispatchMethod));
    }

    /***
     * <p>Check if {@link Listener} is registered to Dispatcher.</p>
     * @param registeredListener {@link RegisteredListener} that holds listening content
     * @return  true if dispatcher holds record of {@link RegisteredListener} else false
     */
    public boolean isListenerRegistered(RegisteredListener registeredListener) {
        return registeredListeners.stream().anyMatch(l -> l.equals(registeredListener));
    }

    /***
     * <p>Send event object to registered {@link Listener}. This function sends events asynchronously,
     * but wait until all listeners have finished executing the onEvent method.
     * See {@link Listener#onEvent}.</p>
     * @param event Event that the listener is subscribed to
     * @param dispatcher Dispatcher object that sends the events
     * @param foo Function to decide to which listener the event should be sent
     */
    protected void dispatch(Event event, AbstractDispatcher dispatcher, Function<RegisteredListener, Boolean> foo) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<RegisteredListener> simpleListeners = registeredListeners.stream().filter(l -> !(l.listener() instanceof ContextEditingListener<?>)).toList();
        List<RegisteredListener> contextEditingListeners = registeredListeners.stream().filter(l -> l.listener() instanceof ContextEditingListener<?>).toList();
        for (RegisteredListener registeredListener : simpleListeners) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                if (foo.apply(registeredListener)) {
                    log.trace("Sending event with message {} synchronously", event.getMessage());
                    registeredListener.listener().onEvent(event, dispatcher);
                }
            });
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        Event updatedEvent = event;
        for (RegisteredListener registeredListener : contextEditingListeners) {
            if (foo.apply(registeredListener)) {
                updatedEvent = ((ContextEditingListener<Event>) registeredListener.listener()).onContextEditingEvent(updatedEvent, dispatcher);
            }
        }
    }

    /***
     * <p>Send event object to registered {@link Listener}.This function dispatches events asynchronously
     * and does not wait for the execution of the onAsyncEvent method to complete.
     * See {@link Listener#onAsyncEvent}.</p>
     * @param event Event that the listener is subscribed to
     * @param dispatcher Dispatcher object that sends the events
     * @param foo Function to decide to which listener the event should be sent
     */
    protected void dispatchAsync(Event event, AbstractDispatcher dispatcher, Function<RegisteredListener, Boolean> foo) {
        for (RegisteredListener registeredListener : registeredListeners) {
            CompletableFuture.runAsync(() -> {
                if (foo.apply(registeredListener)) {
                    log.trace("Sending event with message {} asynchronously", event.getMessage());
                    registeredListener.listener().onAsyncEvent(event, dispatcher);
                }
            });
        }
    }

    protected String getName() {
        return this.getClass().getSimpleName();
    }

    private boolean isRegistrationAllowed(RegisteredListener registeredListener) {
        return allowedActions.contains(registeredListener.eventAction());
    }

}
