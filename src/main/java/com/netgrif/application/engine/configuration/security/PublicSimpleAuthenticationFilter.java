package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.constants.AnonymIdentityConstants;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * todo javadoc
 */
@Slf4j
public class PublicSimpleAuthenticationFilter extends PublicAuthenticationFilter  {


    public PublicSimpleAuthenticationFilter(IIdentityService identityService, ProviderManager authenticationManager,
                                            AnonymousAuthenticationProvider provider, String[] urls, String[] exceptions,
                                            IRoleService roleService) {
        super(authenticationManager, provider, urls, exceptions, identityService, roleService);
    }

    /**
     * todo javadoc
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isPublicApi(request.getRequestURI())) {
            Identity identity = getAnonymousIdentityWithUser();
            authenticate(request, identity.toSession());
            log.info("Anonymous identity was authenticated.");
        }
        filterChain.doFilter(request, response);
    }

    /**
     * todo javadoc
     */
    @Override
    protected Identity getAnonymousIdentityWithUser() {
        Optional<Identity> anonymIdentityOpt = identityService.findByUsername(AnonymIdentityConstants.defaultUsername());
        if (anonymIdentityOpt.isPresent()) {
            return anonymIdentityOpt.get();
        } else  {
            throw new IllegalStateException(String.format("Default anonymous identity with username [%s] doesn't exist",
                    AnonymIdentityConstants.defaultUsername()));
        }
    }
}
