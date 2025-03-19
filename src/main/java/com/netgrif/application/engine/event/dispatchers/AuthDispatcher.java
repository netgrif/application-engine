package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.core.event.dispatchers.common.AbstractDispatcher;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuthDispatcher extends AbstractDispatcher {
    protected AuthDispatcher() {
        super(Set.of(AuthenticationSuccessEvent.class, AuthenticationFailureBadCredentialsEvent.class));
    }

    @EventListener
    public void handleAuthSuccessEvent(AuthenticationSuccessEvent event) {
        dispatch(event);
    }

    @EventListener
    public void handleAsyncAuthSuccessEvent(AuthenticationSuccessEvent event) {
        dispatchAsync(event);
    }

    @EventListener
    public void handleAuthFailBadCredentialsEvent(AuthenticationFailureBadCredentialsEvent event) {
        dispatch(event);
    }


    @EventListener
    public void handleAsyncAuthFailBadCredentialsEvent(AuthenticationFailureBadCredentialsEvent event) {
        dispatchAsync(event);
    }
}
