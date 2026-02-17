package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.auth.domain.Impersonator;
import com.netgrif.application.engine.auth.repository.ImpersonatorRepository;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

public class ImpersonationRequestFilter extends OncePerRequestFilter {

    public static final Logger log = LoggerFactory.getLogger(ImpersonationRequestFilter.class);

    private final ImpersonatorRepository impersonatorRepository;

    public ImpersonationRequestFilter(ImpersonatorRepository impersonatorRepository) {
        this.impersonatorRepository = impersonatorRepository;
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
            Optional<Impersonator> imp = impersonatorRepository.findById(loggedUser.getStringId());
            if (loggedUser.isImpersonating() && (imp.isEmpty() || !isValid(imp.get()))) {
                imp.ifPresent(imper -> impersonatorRepository.deleteById(loggedUser.getStringId()));
                logout(servletRequest, servletResponse);
            }
        } catch (Exception e) {
            log.error("ImpersonationRequestFilter error " + e.getMessage(), e);
        }
    }

    protected void handleImpersonated(LoggedUser loggedUser, HttpServletRequest servletRequest) {
        try {
            log.debug("Filtering request " + servletRequest.getRequestURI() + ", " + loggedUser.getUsername());
            Optional<Impersonator> impersonatorObject = impersonatorRepository.findByImpersonatedId(loggedUser.getStringId());
            impersonatorObject.ifPresent(impersonatorRepository::delete);
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
