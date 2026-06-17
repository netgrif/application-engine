package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.auth.service.SecurityContextService;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Request filter for filtering out user tokens
 */
@Slf4j
public class SecurityContextFilter extends OncePerRequestFilter {

    /**
     * Security context service for managing user tokens
     */
    private final SecurityContextService securityContextService;

    public SecurityContextFilter(SecurityContextService securityContextService) {
        this.securityContextService = securityContextService;
    }

    /**
     * Filter function that helps to filter out the user token and call corresponding service when it is needed
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (securityContextService.isAuthenticatedPrincipalLoggedUser())
            securityContextService.forceReloadSecurityContext((LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        filterChain.doFilter(request, response);
    }
}
