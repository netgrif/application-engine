package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.events.EventAction;
import com.netgrif.application.engine.event.events.user.UserLoginEvent;
import com.netgrif.application.engine.event.events.user.UserLogoutEvent;
import com.netgrif.application.engine.event.events.user.UserRegistrationEvent;
import com.netgrif.application.engine.event.events.user.UserRoleChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserDispatcher extends AbstractDispatcher {

    public UserDispatcher() {
        super(Set.of(EventAction.USER_LOGIN, EventAction.USER_LOGOUT, EventAction.USER_REGISTER, EventAction.USER_ROLE_CHANGE));
    }

    @EventListener
    public void handleUserLoginEvent(UserLoginEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.USER_LOGIN
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncUserLoginEvent(UserLoginEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.USER_LOGIN
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleUserLogoutEvent(UserLogoutEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.USER_LOGOUT
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncUserLogoutEvent(UserLogoutEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.USER_LOGOUT
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleUserRegisterEvent(UserRegistrationEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.USER_REGISTER
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncUserRegisterEvent(UserRegistrationEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.USER_REGISTER
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }

    @EventListener
    public void handleUserRoleChangeEvent(UserRoleChangeEvent event) {
        dispatch(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.USER_ROLE_CHANGE
                        && registeredListener.dispatchMethod() == DispatchMethod.SYNC);
    }

    @EventListener
    public void handleAsyncUseRoleChangeEvent(UserRoleChangeEvent event) {
        dispatchAsync(event, this, (RegisteredListener registeredListener) ->
                registeredListener.eventAction() == EventAction.USER_ROLE_CHANGE
                        && registeredListener.dispatchMethod() == DispatchMethod.ASYNC);
    }
}
