package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.constants.AnonymIdentityConstants;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
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
import java.util.Set;

/**
 * todo javadoc
 */
@Slf4j
public class PublicSimpleAuthenticationFilter extends PublicAuthenticationFilter  {
    private static final String USERNAME = "anonymous";

    public PublicSimpleAuthenticationFilter(IIdentityService identityService, IRoleService roleService, ProviderManager authenticationManager, AnonymousAuthenticationProvider provider,
                                            Authority anonymousAuthority, String[] urls, String[] exceptions) {
        super(identityService, roleService, authenticationManager, provider, anonymousAuthority, urls, exceptions);
    }

    /**
     * todo javadoc
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isPublicApi(request.getRequestURI())) {
            Identity identity = createAnonymousIdentityWithActor();
            authenticate(request, identity.toSession());
            log.info("Anonymous identity was authenticated.");
        }
        filterChain.doFilter(request, response);
    }

    /**
     * todo javadoc
     */
    @Override
    protected Identity createAnonymousIdentityWithActor() {
        String username = AnonymIdentityConstants.usernameOf(USERNAME);
        Optional<Identity> anonymIdentityOpt = identityService.findByUsername(AnonymIdentityConstants.usernameOf(USERNAME));
        if (anonymIdentityOpt.isPresent()) {
            return anonymIdentityOpt.get();
        }

        Identity anonymIdentity = identityService.createWithDefaultActor(IdentityParams.with()
                        .username(new TextField(username))
                        .firstname(new TextField(AnonymIdentityConstants.FIRSTNAME))
                        .lastname(new TextField(AnonymIdentityConstants.LASTNAME))
                .build());

        roleService.assignRolesToActor(anonymIdentity.getMainActorId(), Set.of(roleService.findAnonymousRole().getStringId()));
        // todo 2058 app role

        return anonymIdentity;
    }
}
