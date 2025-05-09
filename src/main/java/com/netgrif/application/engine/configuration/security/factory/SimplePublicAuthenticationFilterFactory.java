package com.netgrif.application.engine.configuration.security.factory;

import com.netgrif.application.engine.authentication.domain.IdentityProperties;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.PublicSimpleAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "nae.public", name = "strategy", havingValue = "simple")
public class SimplePublicAuthenticationFilterFactory extends PublicAuthenticationFilterFactory {

    public SimplePublicAuthenticationFilterFactory(IIdentityService identityService, IRoleService roleService,
                                                   NaeAuthProperties naeAuthProperties) {
        super(identityService, roleService, naeAuthProperties);
    }

    @Override
    public PublicAuthenticationFilter doCreateFilter(ProviderManager authManager) {
        return new PublicSimpleAuthenticationFilter(identityService, authManager,
                new AnonymousAuthenticationProvider(IdentityProperties.ANONYMOUS_AUTH_KEY),
                naeAuthProperties.getServerPatterns(), naeAuthProperties.getAnonymousExceptions(), roleService);
    }
}
