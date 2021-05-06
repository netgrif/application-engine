package com.netgrif.workflow.configuration;

import com.netgrif.workflow.auth.service.OauthUserService;
import com.netgrif.workflow.auth.service.interfaces.IOauthUserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuthUserServiceConfiguration {

    @Bean
    @ConditionalOnExpression("${nae.oauth.enabled}")
    public IOauthUserService userService() {
        return new OauthUserService();
    }

}
