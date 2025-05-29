package com.netgrif.application.engine.configuration.security.factory;

import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ProviderManager;

@RequiredArgsConstructor
public abstract class PublicAuthenticationFilterFactory {

    protected final IIdentityService identityService;
    protected final IRoleService roleService;
    protected final NaeAuthProperties naeAuthProperties;
    
    private PublicAuthenticationFilter singletonFilter;

    /**
     * todo javadoc
     * */
    protected abstract PublicAuthenticationFilter doCreateFilter(ProviderManager authManager);

    /**
     * todo javadoc
     * */
    public PublicAuthenticationFilter createFilter(ProviderManager authManager) {
        if (this.singletonFilter != null) {
            return this.singletonFilter;
        }

        this.singletonFilter = doCreateFilter(authManager);
        return this.singletonFilter;
    }
}
