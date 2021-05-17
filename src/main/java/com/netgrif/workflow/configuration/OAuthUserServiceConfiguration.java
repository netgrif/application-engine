package com.netgrif.workflow.configuration;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.oauth.domain.RemoteUserResource;
import com.netgrif.workflow.oauth.service.OAuthUserService;
import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuthUserServiceConfiguration {

    @Bean
    @ConditionalOnExpression("${nae.oauth.enabled}")
    public IUserService userService(IRemoteUserResourceService<RemoteUserResource> remoteUserResourceService) {
        return new OAuthUserService(remoteUserResourceService);
    }

}
