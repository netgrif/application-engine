package com.netgrif.application.engine.objects.event.dispatchers.common;

import java.util.EventObject;

public interface DispatcherRegistry {
    AbstractDispatcher getDispatcher(Class<? extends EventObject> event);

    AbstractDispatcher getDispatcher(String dispatcherId);

    boolean containsDispatcher(String dispatcherId);

    boolean containsDispatcher(Class<? extends EventObject> event);
}
