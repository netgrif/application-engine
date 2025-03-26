package com.netgrif.application.engine.configuration.security.factory;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import org.springframework.security.authentication.ProviderManager;

import java.util.HashSet;

public abstract class PublicAuthenticationFilterFactory {

    private final IAuthorityService authorityService;
    
    protected final IIdentityService identityService;
    protected final IRoleService roleService;
    protected final NaeAuthProperties naeAuthProperties;
    
    private PublicAuthenticationFilter singletonFilter;

    public PublicAuthenticationFilterFactory(IAuthorityService authorityService, IIdentityService identityService,
                                             IRoleService roleService, NaeAuthProperties naeAuthProperties) {
        this.authorityService = authorityService;
        this.identityService = identityService;
        this.roleService = roleService;
        this.naeAuthProperties = naeAuthProperties;
    }

    /**
     * todo javadoc
     * */
    protected abstract PublicAuthenticationFilter doCreateFilter(ProviderManager authManager, Authority authority);

    /**
     * todo javadoc
     * */
    public PublicAuthenticationFilter createFilter(ProviderManager authManager) {
        if (this.singletonFilter != null) {
            return this.singletonFilter;
        }
        Authority authority = authorityService.getOrCreate(Authority.anonymous);
        authority.setUsers(new HashSet<>());
        this.singletonFilter = doCreateFilter(authManager, authority);
        return this.singletonFilter;
    }
}
