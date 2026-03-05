package com.netgrif.application.engine.adapter.spring.event;

import com.netgrif.application.engine.objects.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.objects.event.dispatchers.common.DispatcherRegistry;
import org.springframework.stereotype.Component;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DispatcherRegistryImpl implements DispatcherRegistry {

    private final Map<Class<? extends EventObject>, AbstractDispatcher> byEvent ;

    private final Map<String, AbstractDispatcher> byName = new HashMap<>();

    public DispatcherRegistryImpl(List<AbstractDispatcher> dispatchers) {
        this.byEvent = new HashMap<>();
        dispatchers.forEach(d ->
                d.getAllowedEvents().forEach(e -> byEvent.put(e, d)));
        dispatchers.forEach(d -> byName.put(d.getName(), d));

    }

    @Override
    public AbstractDispatcher getDispatcher(Class<? extends EventObject> event) {
        return byEvent.get(event);
    }


    @Override
    public AbstractDispatcher getDispatcher(String dispatcherId) {
        return byName.get(dispatcherId);
    }

    @Override
    public boolean containsDispatcher(String dispatcherId) {
        return byName.containsKey(dispatcherId);
    }

    @Override
    public boolean containsDispatcher(Class<? extends EventObject> event) {
        return byEvent.containsKey(event);
    }

}
