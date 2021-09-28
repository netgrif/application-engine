package com.netgrif.workflow.auth.service;


import com.netgrif.workflow.auth.service.interfaces.IAfterRegistrationAuthService;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.http.HttpServletRequest;

@Data
public class AfterRegistrationAuthService implements IAfterRegistrationAuthService {

    private ProviderManager authenticationManager;
    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;

    public AfterRegistrationAuthService(ProviderManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.authenticationDetailsSource = new WebAuthenticationDetailsSource();
    }

    @Override
    public void authenticateWithUsernameAndPassword(String username, String password) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
        authToken.setDetails(SecurityContextHolder.getContext().getAuthentication().getDetails());
        Authentication authResult = this.authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authResult);
    }

    @Override
    public void logoutAfterRegistrationFinished() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }


}
