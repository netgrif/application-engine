package com.netgrif.application.engine.event.listeners;

import com.netgrif.application.engine.auth.service.interfaces.ILoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AuthEventListener {

    @Autowired
    private ILoginAttemptService loginAttemptService;

    @EventListener
    public void onAuthenticationFailureBadCredentialsEvent(final AuthenticationFailureBadCredentialsEvent e) {
        WebAuthenticationDetails auth = (WebAuthenticationDetails) e.getAuthentication().getDetails();

        loginAttemptService.loginFailed(auth.getRemoteAddress());
    }

    @EventListener
    public void onAuthenticationSuccessEvent(final AuthenticationSuccessEvent e) {
        WebAuthenticationDetails auth = (WebAuthenticationDetails) e.getAuthentication().getDetails();

        loginAttemptService.loginSucceeded(auth.getRemoteAddress());
    }
}
