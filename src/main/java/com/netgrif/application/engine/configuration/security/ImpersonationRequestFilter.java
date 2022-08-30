package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ImpersonationRequestFilter extends OncePerRequestFilter {

    public static final Logger log = LoggerFactory.getLogger(ImpersonationRequestFilter.class);

    @Override
    public void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            LoggedUser loggedUser = getPrincipal();
            if (loggedUser != null) {
                handleImpersonated(loggedUser, servletRequest);
                handleImpersonator(loggedUser, servletRequest, servletResponse);
            }
        } catch (Exception e) {
            log.error("Filter error", e);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    void handleImpersonator(LoggedUser loggedUser, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        try {
            if (loggedUser.isImpersonating()) {

            }
        } catch (Exception e) {
            log.error("ImpersonationRequestFilter error " + e.getMessage(), e);
        }
    }

    private void handleImpersonated(LoggedUser loggedUser, HttpServletRequest servletRequest) {
        try {
            log.debug("Filtering request " + servletRequest.getRequestURI() + ", " + loggedUser.getUsername());

        } catch (Exception e) {
            log.error("Failed to resolve impersonators for " + loggedUser.getUsername() + ", " + e.getMessage(), e);
        }
    }

    private LoggedUser getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof LoggedUser)) {
            log.warn(principal + " is not an instance of LoggedUser");
            return null;
        }

        return (LoggedUser) auth.getPrincipal();
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, null);
    }


}
