package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.constants.AnonymIdentityConstants;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

import java.util.Optional;
import java.util.Set;

/**
 * todo javadoc
 */
@Slf4j
public class PublicAdvancedAuthenticationFilter extends PublicJwtAuthenticationFilter {

    public PublicAdvancedAuthenticationFilter(IIdentityService identityService, IRoleService roleService, ProviderManager authenticationManager,
                                              AnonymousAuthenticationProvider provider, Authority anonymousAuthority,
                                              String[] urls, String[] exceptions, IJwtService jwtService) {
        super(identityService, roleService, authenticationManager, provider, anonymousAuthority, urls, exceptions, jwtService);
    }

    /**
     * todo javadoc
     */
    @Override
    protected Identity createAnonymousIdentityWithActor() {
        String hash = new ObjectId().toString();

        Optional<Identity> anonymIdentityOpt = identityService.findByUsername(AnonymIdentityConstants.usernameOf(hash));
        if (anonymIdentityOpt.isPresent()) {
            return anonymIdentityOpt.get();
        }

        Identity identity = identityService.createWithDefaultActor(IdentityParams.with()
                .username(new TextField(AnonymIdentityConstants.usernameOf(hash)))
                .firstname(new TextField(AnonymIdentityConstants.FIRSTNAME))
                .lastname(new TextField(AnonymIdentityConstants.LASTNAME))
                .password(new TextField("n/a"))
                .build());

        roleService.assignRolesToActor(identity.getMainActorId(), Set.of(roleService.findAnonymousRole().getStringId()));
        // todo 2058 app role

        return identity;
    }
}
