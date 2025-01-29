package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.events.data.GetDataEvent;
import com.netgrif.application.engine.event.events.data.SetDataEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataDispatcher extends AbstractDispatcher {

    public DataDispatcher() {
        super(Set.of(GetDataEvent.class, SetDataEvent.class));
    }

    @EventListener
    public void handleGetDataEvent(GetDataEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncGetDataEvent(GetDataEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleSetDataEvent(SetDataEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncSetDataEvent(SetDataEvent event) {
        dispatchAsync(event);
    }

}
