package com.netgrif.workflow.configuration;

import com.netgrif.workflow.auth.service.RegistrationService;
import com.netgrif.workflow.auth.service.UserDetailsServiceImpl;
import com.netgrif.workflow.auth.service.UserService;
import com.netgrif.workflow.auth.service.interfaces.IRegistrationService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.responsebodies.IUserFactory;
import com.netgrif.workflow.auth.web.responsebodies.UserFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class UserServiceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IUserService userService() {
        return new UserService();
    }

    @Bean
    @ConditionalOnMissingBean
    public IRegistrationService registrationService() {
        return new RegistrationService();
    }

    @Bean
    @ConditionalOnMissingBean
    public IUserFactory userFactory() {
        return new UserFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }
}
