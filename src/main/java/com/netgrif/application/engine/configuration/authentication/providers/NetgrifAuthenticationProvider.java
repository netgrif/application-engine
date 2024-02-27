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

    protected UserDetailsService userDetailsService;

    protected HttpServletRequest request;

    protected ApplicationEventPublisher publisher;

    protected ILoginAttemptService loginAttemptService;

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Autowired
    public void setPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Autowired
    public void setLoginAttemptService(ILoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

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
