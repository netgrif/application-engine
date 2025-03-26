package com.netgrif.application.engine.configuration.security.factory;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.IdentityProperties;
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.PublicSimpleAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("${nae.public.strategy}.equals(#(T(com.netgrif.application.engine.authentication.domain.PublicStrategy).SIMPLE.name()))")
public class SimplePublicAuthenticationFilterFactory extends PublicAuthenticationFilterFactory {

    public SimplePublicAuthenticationFilterFactory(IAuthorityService authorityService, IIdentityService identityService,
                                                   IRoleService roleService, NaeAuthProperties naeAuthProperties) {
        super(authorityService, identityService, roleService, naeAuthProperties);
    }

    @Override
    public PublicAuthenticationFilter doCreateFilter(ProviderManager authManager, Authority authority) {
        return new PublicSimpleAuthenticationFilter(identityService, roleService, authManager,
                new AnonymousAuthenticationProvider(IdentityProperties.ANONYMOUS_AUTH_KEY), authority,
                naeAuthProperties.getServerPatterns(), naeAuthProperties.getAnonymousExceptions());
    }
}
