package com.netgrif.application.engine.adapter.spring.plugin.annotations;

import com.netgrif.application.engine.objects.event.dispatchers.common.AbstractDispatcher;

import java.util.EventObject;

public @interface ListenerFilter {
    Class<? extends EventObject> eventType();
    AbstractDispatcher.DispatchMethod dispatchMethod();
}
