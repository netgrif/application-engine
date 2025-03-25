package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.impersonation.domain.Impersonator;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class ImpersonationRequestFilter extends OncePerRequestFilter {

    private final IImpersonationService impersonationService;

    public ImpersonationRequestFilter(IImpersonationService impersonationService) {
        this.impersonationService = impersonationService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            Identity identity = getPrincipal();
            if (identity != null) {
                handleImpersonated(identity, servletRequest);
                handleImpersonator(identity, servletRequest, servletResponse);
            }
        } catch (Exception e) {
            log.error("Filter error", e);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    protected void handleImpersonator(Identity identity, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        try {
            if (!identity.isImpersonating()) {
                return;
            }
            Optional<Impersonator> imp = impersonationService.findImpersonator(identity.getId());
            if (identity.isImpersonating() && (imp.isEmpty() || !isValid(imp.get()))) {
                imp.ifPresent(imper -> impersonationService.removeImpersonator(identity.getId()));
                logout(servletRequest, servletResponse);
            }
        } catch (Exception e) {
            log.error("ImpersonationRequestFilter error {}", e.getMessage(), e);
        }
    }

    protected void handleImpersonated(Identity identity, HttpServletRequest servletRequest) {
        try {
            log.debug("Filtering request {}, {}", servletRequest.getRequestURI(), identity.getUsername());
            impersonationService.removeImpersonatorByImpersonated(identity.getId());
        } catch (Exception e) {
            log.error("Failed to resolve impersonators for {}, {}", identity.getUsername(), e.getMessage(), e);
        }
    }

    protected boolean isValid(Impersonator impersonator) {
        if (impersonator.getImpersonatingUntil() == null) {
            return true;
        }
        return !LocalDateTime.now().isAfter(impersonator.getImpersonatingUntil());
    }

    protected Identity getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof Identity)) {
            log.warn("{} is not an instance of LoggedUser", principal);
            return null;
        }

        return (Identity) auth.getPrincipal();
    }

    protected void logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, null);
    }


}
