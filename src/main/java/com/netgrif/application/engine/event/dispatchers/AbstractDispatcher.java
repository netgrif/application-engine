package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.event.events.EventAction;
import com.netgrif.application.engine.event.listeners.Listener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
public abstract class AbstractDispatcher {

    //TODO: 1. Volanie metod dispatch a dispatch ma byt asynchronne v oboch pripadoch
    //      2. Rozdiel je iba v tom ze dispatch caka na skonecnie vykonavania instrukcii v Listeneroch a
    //          dispatchAsync nie

    private final Set<RegisteredListener> registeredListeners;

    private final Set<EventAction> allowedActions;

    protected AbstractDispatcher(Set<EventAction> allowedActions) {
        this.allowedActions = allowedActions;
        this.registeredListeners = new HashSet<>();
    }

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

    public boolean isListenerRegistered(Listener listener, EventAction eventAction, DispatchMethod dispatchMethod) {
        return isListenerRegistered(new RegisteredListener(listener, eventAction, dispatchMethod));
    }

    private boolean isListenerRegistered(RegisteredListener registeredListener) {
        return registeredListeners.stream().anyMatch(l -> l.equals(registeredListener));
    }

    protected void dispatch(Event event, AbstractDispatcher dispatcher, Function<RegisteredListener, Boolean> foo) {
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (RegisteredListener registeredListener : registeredListeners) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                if (foo.apply(registeredListener)) {
                    log.info("Sending event {} synchronously", event.getMessage());
                    registeredListener.listener().onEvent(event, dispatcher);
                }
            });
            //custom executor ?
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
//        executorService.shutdown();
    }

    protected void dispatchAsync(Event event, AbstractDispatcher dispatcher, Function<RegisteredListener, Boolean> foo) {
        for (RegisteredListener registeredListener : registeredListeners) {
            CompletableFuture.runAsync(() -> {
                if (foo.apply(registeredListener)) {
                    log.info("Sending event {} asynchronously", event.getMessage());
                    registeredListener.listener().onEvent(event, dispatcher);
                }
            });
            //custom executor ?
        }
    }

    private boolean isRegistrationAllowed(RegisteredListener registeredListener) {
        return allowedActions.contains(registeredListener.eventAction);
    }

    protected record RegisteredListener(Listener listener, EventAction eventAction, DispatchMethod dispatchMethod) {
    }

    protected String getName() {
        return this.getClass().getSimpleName();
    }

    public enum DispatchMethod {
        SYNC,
        ASYNC
    }

}
