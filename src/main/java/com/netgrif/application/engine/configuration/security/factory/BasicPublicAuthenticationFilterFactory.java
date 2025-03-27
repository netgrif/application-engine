package com.netgrif.application.engine.configuration.security.factory;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.IdentityProperties;
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.PublicBasicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("${nae.public.strategy}.equals(#(T(com.netgrif.application.engine.authentication.domain.PublicStrategy).BASIC.name()))")
public class BasicPublicAuthenticationFilterFactory extends PublicAuthenticationFilterFactory {

    private final IJwtService jwtService;
    private final IActorService actorService;

    public BasicPublicAuthenticationFilterFactory(IAuthorityService authorityService, IIdentityService identityService,
                                                  IRoleService roleService, NaeAuthProperties naeAuthProperties, IJwtService jwtService, IActorService actorService) {
        super(authorityService, identityService, roleService, naeAuthProperties);
        this.jwtService = jwtService;
        this.actorService = actorService;
    }

    @Override
    protected PublicAuthenticationFilter doCreateFilter(ProviderManager authManager, Authority authority) {
        return new PublicBasicAuthenticationFilter(identityService, roleService, authManager,
                new AnonymousAuthenticationProvider(IdentityProperties.ANONYMOUS_AUTH_KEY), authority,
                naeAuthProperties.getServerPatterns(), naeAuthProperties.getAnonymousExceptions(), jwtService, actorService);
    }
}
