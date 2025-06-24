package com.netgrif.application.engine.objects.plugin.domain;

import com.netgrif.application.engine.objects.event.dispatchers.common.AbstractDispatcher;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.EventObject;

/**
 * Domain class for listener filters. This filter define, which listener will call methods of this plugin.
 * */
@Data
public class ListenerFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 892495676687111427L;

    private Class<? extends EventObject> eventType;

    private AbstractDispatcher.DispatchMethod dispatchMethod;

    public ListenerFilter() {
    }

    @Builder
    public ListenerFilter(Class<? extends EventObject> eventType, AbstractDispatcher.DispatchMethod dispatchMethod) {
        this.eventType = eventType;
        this.dispatchMethod = dispatchMethod;
    }
}
