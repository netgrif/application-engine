package com.netgrif.application.engine.configuration.security.factory;

import com.netgrif.application.engine.authentication.domain.IdentityProperties;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.PublicBasicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "nae.public", name = "strategy", havingValue = "basic")
public class BasicPublicAuthenticationFilterFactory extends PublicAuthenticationFilterFactory {

    private final IJwtService jwtService;
    private final IUserService userService;

    public BasicPublicAuthenticationFilterFactory(IIdentityService identityService, IRoleService roleService,
                                                  NaeAuthProperties naeAuthProperties, IJwtService jwtService, IUserService userService) {
        super(identityService, roleService, naeAuthProperties);
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected PublicAuthenticationFilter doCreateFilter(ProviderManager authManager) {
        return new PublicBasicAuthenticationFilter(identityService, authManager,
                new AnonymousAuthenticationProvider(IdentityProperties.ANONYMOUS_AUTH_KEY),
                naeAuthProperties.getServerPatterns(), naeAuthProperties.getAnonymousExceptions(), jwtService,
                userService, roleService);
    }
}
