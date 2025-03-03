package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.authentication.service.RegistrationService;
import com.netgrif.application.engine.authentication.service.UserDetailsServiceImpl;
import com.netgrif.application.engine.authentication.service.UserService;
import com.netgrif.application.engine.authentication.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
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
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }
}
