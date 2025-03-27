package com.netgrif.application.engine.configuration.security.factory;

import com.netgrif.application.engine.authentication.domain.IdentityProperties;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.ApplicationRole;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.security.PublicAdvancedAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("${nae.public.strategy}.equals(#(T(com.netgrif.application.engine.authentication.domain.PublicStrategy).ADVANCED.name()))")
public class AdvancedPublicAuthenticationFilterFactory extends PublicAuthenticationFilterFactory {

    private final IJwtService jwtService;

    public AdvancedPublicAuthenticationFilterFactory(ApplicationRoleRunner applicationRoleRunner, IIdentityService identityService,
                                                     IRoleService roleService, NaeAuthProperties naeAuthProperties, IJwtService jwtService) {
        super(applicationRoleRunner, identityService, roleService, naeAuthProperties);
        this.jwtService = jwtService;
    }

    @Override
    protected PublicAuthenticationFilter doCreateFilter(ProviderManager authManager, ApplicationRole anonymousAppRole,
                                                        ProcessRole anonymousProcessRole) {
        return new PublicAdvancedAuthenticationFilter(identityService, roleService, authManager,
                new AnonymousAuthenticationProvider(IdentityProperties.ANONYMOUS_AUTH_KEY), anonymousAppRole, anonymousProcessRole,
                naeAuthProperties.getServerPatterns(), naeAuthProperties.getAnonymousExceptions(), jwtService);
    }
}
