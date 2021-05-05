package com.netgrif.workflow.configuration;

import com.netgrif.workflow.auth.service.OauthUserService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${nae.oauth.enabled}")
public class UserServiceConfiguration {

    @Bean
    public IUserService userService() {
        return new OauthUserService();
    }
}
