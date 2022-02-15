package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.services.interfaces.IEventSystemService;
import events.IDispatcher;
import events.IEvent;
import events.ISubscriber;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class AbstractDispatcher implements IDispatcher {

    protected final List<ISubscriber> subscribers;

    protected AbstractDispatcher(IEventSystemService eventSystemService) {
        this.subscribers = new ArrayList<>();
        eventSystemService.registerDispatcher(this);
    }

    @Override
    public abstract String getId();

    @Override
    public void registerSubscriber(ISubscriber subscriber) {
        if (subscriber != null)
            subscribers.add(subscriber);
    }

    @Override
    public void listen(IEvent event) {
        subscribers.forEach(s -> System.out.println(s.onEvent(event)));
    }

    @Override
    public <T> void listen(Class<T> eventClass, T event) {
    }

    protected List<ISubscriber> getSubscribers() {
        return this.subscribers;
    }
}
