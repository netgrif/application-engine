package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.*;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Slf4j
public abstract class PublicAuthenticationFilter extends OncePerRequestFilter {

    protected final IIdentityService identityService;
    protected final IRoleService roleService;
    protected final ProviderManager authenticationManager;
    protected final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    protected final String[] anonymousAccessUrls;
    protected final String[] exceptions;

    public PublicAuthenticationFilter(ProviderManager authenticationManager, AnonymousAuthenticationProvider provider,
                                      String[] urls, String[] exceptions, IIdentityService identityService, IRoleService roleService) {
        this.identityService = identityService;
        this.authenticationManager = authenticationManager;
        this.roleService = roleService;
        this.authenticationManager.getProviders().add(provider);
        this.anonymousAccessUrls = urls;
        this.exceptions = exceptions;
    }

    protected abstract Identity getAnonymousIdentityWithUser();

    protected void authenticate(HttpServletRequest request, LoggedIdentity loggedIdentity) {
        AnonymousAuthenticationToken authRequest = new AnonymousAuthenticationToken(
                IdentityProperties.ANONYMOUS_AUTH_KEY,
                loggedIdentity,
                Set.of(roleService.findApplicationRoleByImportId(ApplicationRoleRunner.ANONYMOUS_APP_ROLE))
        );
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        Authentication authResult = this.authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authResult);
    }

    /**
     * todo javadoc
     */
    protected boolean isPublicApi(String path) {
        for (String url : this.anonymousAccessUrls) {
            if (path.matches(url.replace("*", ".*?"))) {
                for (String ex : this.exceptions) {
                    if (path.matches(ex.replace("*", ".*?"))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
