package com.netgrif.workflow.configuration;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.responsebodies.IUserFactory;
import com.netgrif.workflow.auth.web.responsebodies.OAuthUserFactory;
import com.netgrif.workflow.oauth.domain.RemoteGroupResource;
import com.netgrif.workflow.oauth.domain.RemoteUserResource;
import com.netgrif.workflow.oauth.service.OAuthUserMapper;
import com.netgrif.workflow.oauth.service.OAuthUserService;
import com.netgrif.workflow.oauth.service.interfaces.IOauthUserMapper;
import com.netgrif.workflow.oauth.service.interfaces.IRemoteGroupResourceService;
import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${nae.oauth.enabled}")
public class OAuthUserServiceConfiguration {

    @Bean
    public IUserService userService(IRemoteUserResourceService<RemoteUserResource> remoteUserResourceService,
                                    IRemoteGroupResourceService<RemoteGroupResource, RemoteUserResource> remoteGroupResourceService) {
        return new OAuthUserService(remoteUserResourceService, remoteGroupResourceService);
    }

    @Bean
    @ConditionalOnMissingBean
    public IOauthUserMapper oauthUserMapper() {
        return new OAuthUserMapper();
    }

    @Bean
    public IUserFactory userFactory() {
        return new OAuthUserFactory();
    }

}
