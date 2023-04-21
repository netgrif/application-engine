package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Request filter for filtering out user tokens
 * */
@Slf4j
public class SecurityContextFilter extends OncePerRequestFilter {

    /**
     * Security context service for managing user tokens
     * */
    private final ISecurityContextService securityContextService;

    public SecurityContextFilter(ISecurityContextService securityContextService) {
        this.securityContextService = securityContextService;
    }

    /**
     * Filter function that helps to filter out the user token and call corresponding service when it is needed
     * */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (securityContextService.isAuthenticatedPrincipalLoggedUser())
            securityContextService.forceReloadSecurityContext((LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        filterChain.doFilter(request, response);
    }
}
