package com.netgrif.application.engine.configuration.security.factory;

import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.ApplicationRole;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ProviderManager;

@RequiredArgsConstructor
public abstract class PublicAuthenticationFilterFactory {

    private final ApplicationRoleRunner applicationRoleRunner;

    protected final IIdentityService identityService;
    protected final IRoleService roleService;
    protected final NaeAuthProperties naeAuthProperties;
    
    private PublicAuthenticationFilter singletonFilter;

    /**
     * todo javadoc
     * */
    protected abstract PublicAuthenticationFilter doCreateFilter(ProviderManager authManager, ApplicationRole anonymousAppRole,
                                                                 ProcessRole anonymousProcessRole);

    /**
     * todo javadoc
     * */
    public PublicAuthenticationFilter createFilter(ProviderManager authManager) {
        if (this.singletonFilter != null) {
            return this.singletonFilter;
        }

        ApplicationRole anonymousAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ANONYMOUS_APP_ROLE);
        ProcessRole anonymousProcessRole = (ProcessRole) roleService.findAnonymousRole();

        this.singletonFilter = doCreateFilter(authManager, anonymousAppRole, anonymousProcessRole);
        return this.singletonFilter;
    }
}
