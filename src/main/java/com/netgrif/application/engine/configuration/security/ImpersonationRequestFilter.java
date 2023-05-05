package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.impersonation.domain.Impersonator;
import com.netgrif.application.engine.impersonation.domain.repository.ImpersonatorRepository;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
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
import java.time.LocalDateTime;
import java.util.Optional;

public class ImpersonationRequestFilter extends OncePerRequestFilter {

    public static final Logger log = LoggerFactory.getLogger(ImpersonationRequestFilter.class);

    private final IImpersonationService impersonationService;

    public ImpersonationRequestFilter(IImpersonationService impersonationService) {
        this.impersonationService = impersonationService;
    }

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

    protected void handleImpersonator(LoggedUser loggedUser, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        try {
            if (!loggedUser.isImpersonating()) {
                return;
            }
            Optional<Impersonator> imp = impersonationService.findImpersonator(loggedUser.getId());
            if (loggedUser.isImpersonating() && (imp.isEmpty() || !isValid(imp.get()))) {
                imp.ifPresent(imper -> impersonationService.removeImpersonator(loggedUser.getId()));
                logout(servletRequest, servletResponse);
            }
        } catch (Exception e) {
            log.error("ImpersonationRequestFilter error " + e.getMessage(), e);
        }
    }

    protected void handleImpersonated(LoggedUser loggedUser, HttpServletRequest servletRequest) {
        try {
            log.debug("Filtering request " + servletRequest.getRequestURI() + ", " + loggedUser.getUsername());
            impersonationService.removeImpersonatorByImpersonated(loggedUser.getId());
        } catch (Exception e) {
            log.error("Failed to resolve impersonators for " + loggedUser.getUsername() + ", " + e.getMessage(), e);
        }
    }

    protected boolean isValid(Impersonator impersonator) {
        if (impersonator.getImpersonatingUntil() == null) {
            return true;
        }
        return !LocalDateTime.now().isAfter(impersonator.getImpersonatingUntil());
    }

    protected LoggedUser getPrincipal() {
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

    protected void logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, null);
    }


}
