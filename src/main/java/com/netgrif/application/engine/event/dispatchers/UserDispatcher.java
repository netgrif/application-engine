package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.events.user.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserDispatcher extends AbstractDispatcher {

    public UserDispatcher() {
        super(Set.of(UserLoginEvent.class,
                UserLogoutEvent.class,
                UserRegistrationEvent.class,
                UserRoleChangeEvent.class,
                AdminActionEvent.class
        ));
    }

    @EventListener
    public void handleUserLoginEvent(UserLoginEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncUserLoginEvent(UserLoginEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleUserLogoutEvent(UserLogoutEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncUserLogoutEvent(UserLogoutEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleUserRegisterEvent(UserRegistrationEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncUserRegisterEvent(UserRegistrationEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleUserRoleChangeEvent(UserRoleChangeEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncUseRoleChangeEvent(UserRoleChangeEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleUserRoleChangeEvent(AdminActionEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncUseRoleChangeEvent(AdminActionEvent event) {
        dispatchAsync(event);
    }
}
