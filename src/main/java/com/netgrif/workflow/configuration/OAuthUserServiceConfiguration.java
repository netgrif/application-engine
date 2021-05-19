package com.netgrif.workflow.configuration;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.oauth.domain.RemoteUserResource;
import com.netgrif.workflow.oauth.service.OAuthUserMapper;
import com.netgrif.workflow.oauth.service.OAuthUserService;
import com.netgrif.workflow.oauth.service.interfaces.IOauthUserMapper;
import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${nae.oauth.enabled}")
public class OAuthUserServiceConfiguration {

    @Bean
    public IUserService userService(IRemoteUserResourceService<RemoteUserResource> remoteUserResourceService) {
        return new OAuthUserService(remoteUserResourceService);
    }

    @Bean
    @ConditionalOnMissingBean
    public IOauthUserMapper oauthUserMapper() {
        return new OAuthUserMapper();
    }

}
