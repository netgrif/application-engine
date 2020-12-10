package com.netgrif.workflow.configuration.security.jwt;

import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationProvider extends AnonymousAuthenticationProvider {

    public JwtAuthenticationProvider(String key) {
        super(key);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return super.authenticate(authentication);
    }
}
