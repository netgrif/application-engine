package com.netgrif.application.engine.event.listeners;

import com.netgrif.application.engine.auth.service.interfaces.ILoginAttemptService;
import com.netgrif.application.engine.event.dispatchers.AuthDispatcher;
import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.util.EventObject;
import java.util.Set;

@Component
@Profile("!test")
public class AuthEventListener extends Listener {

    private final ILoginAttemptService loginAttemptService;

    public AuthEventListener(ILoginAttemptService loginAttemptService, AuthDispatcher dispatcher) {
        this.loginAttemptService = loginAttemptService;
        this.registerAll(dispatcher,
                Set.of(AuthenticationFailureBadCredentialsEvent.class,
                        AuthenticationSuccessEvent.class), AbstractDispatcher.DispatchMethod.ASYNC);
    }

    public void onAuthenticationFailureBadCredentialsEvent(final AuthenticationFailureBadCredentialsEvent e) {
        WebAuthenticationDetails auth = (WebAuthenticationDetails) e.getAuthentication().getDetails();

        loginAttemptService.loginFailed(auth.getRemoteAddress());
    }

    public void onAuthenticationSuccessEvent(final AuthenticationSuccessEvent e) {
        WebAuthenticationDetails auth = (WebAuthenticationDetails) e.getAuthentication().getDetails();

        loginAttemptService.loginSucceeded(auth.getRemoteAddress());
    }

    @Override
    public void onAsyncEvent(EventObject event, AbstractDispatcher dispatcher) {
        if (event instanceof AuthenticationSuccessEvent) {
            onAuthenticationSuccessEvent((AuthenticationSuccessEvent) event);
        } else if (event instanceof AuthenticationFailureBadCredentialsEvent) {
            onAuthenticationFailureBadCredentialsEvent((AuthenticationFailureBadCredentialsEvent) event);
        }
    }

    @Override
    public void onEvent(EventObject event, AbstractDispatcher dispatcher) {
        // do nothing
    }


}

