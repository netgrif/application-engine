package com.netgrif.application.engine.history.service.listener;

import com.netgrif.application.engine.event.dispatchers.UserDispatcher;
import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.core.event.events.user.*;
import com.netgrif.application.engine.event.listeners.Listener;
import org.springframework.stereotype.Component;

import java.util.EventObject;
import java.util.Set;

@Component
public class UserEventListener extends Listener {

    public UserEventListener(UserDispatcher dispatcher) {
        this.registerAll(dispatcher,
                Set.of(UserLoginEvent.class,
                        UserLogoutEvent.class,
                        UserRoleChangeEvent.class,
                        UserRegistrationEvent.class,
                        AdminActionEvent.class),
                AbstractDispatcher.DispatchMethod.ASYNC);
    }

    @Override
    public void onEvent(EventObject event, AbstractDispatcher dispatcher) {

        //do nothing
    }

    @Override
    public void onAsyncEvent(EventObject event, AbstractDispatcher dispatcher) {
        //todo
        if (event instanceof UserLoginEvent) {
            // repository.save()
        } else if (event instanceof UserLogoutEvent) {
            // repository.save()
        } else if (event instanceof UserRoleChangeEvent) {
            // repository.save()
        } else if (event instanceof UserRegistrationEvent) {

        } else if (event instanceof AdminActionEvent) {
            // ??
        }
    }
}
