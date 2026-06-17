package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.configuration.properties.ManagementConfigurationProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ActuatorRequestFilter extends OncePerRequestFilter {

    private final String actuatorBasePath ;
    private final String healthPath;

    public ActuatorRequestFilter(WebEndpointProperties webEndpointProperties) {
       actuatorBasePath = webEndpointProperties.getBasePath();
       healthPath = actuatorBasePath + "/health";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (!path.startsWith(actuatorBasePath)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (path.equals(healthPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated(auth)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("HTTP Status 401 - Full authentication is required to access this resource");
            return;
        }

        if (!hasAdminRole(auth)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("HTTP Status 403 - Forbidden");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private boolean hasAdminRole(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }
}
