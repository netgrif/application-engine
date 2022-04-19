package com.netgrif.application.engine.configuration.authentication.providers;

import com.netgrif.application.engine.auth.service.interfaces.ILoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.http.HttpServletRequest;

public abstract class NetgrifAuthenticationProvider implements AuthenticationProvider {


    @Autowired
    protected UserDetailsService userDetailsService;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired
    protected ILoginAttemptService loginAttemptService;


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    public abstract Authentication authenticate(Authentication authentication);

    public abstract boolean supports(Class<?> authentication);

    protected String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

}
