package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.events.EventAction;
import com.netgrif.application.engine.event.events.data.GetDataEvent;
import com.netgrif.application.engine.event.events.data.SetDataEvent;
import org.springframework.context.event.EventListener;

import java.util.Set;

public class DataDispatcher extends AbstractDispatcher {

    public DataDispatcher() {
        super(Set.of(EventAction.DATA_GET, EventAction.DATA_SET));
    }

    @EventListener
    public void handleGetDataEvent(GetDataEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.DATA_GET
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncGetDataEvent(GetDataEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.DATA_GET
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleSetDataEvent(SetDataEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.DATA_SET
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncSetDataEvent(SetDataEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.DATA_SET
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

}
