package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.auth.web.responsebodies.IUserFactory;
import com.netgrif.application.engine.auth.web.responsebodies.UserFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceConfiguration {

    @Value("${nae.ldap.enabled}")
    protected boolean ldapEnabled;

//    @Bean
//    @ConditionalOnMissingBean
//    public IUserService userService() {
//        if (ldapEnabled) {
//            return new LdapUserService();
//        }
//        return new UserService();
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public IRegistrationService registrationService() {
//        return new RegistrationService();
//    }

    @Bean
    public IUserFactory userFactory() {
        return new UserFactory();
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public UserDetailsService userDetailsService() {
//        return new UserDetailsServiceImpl();
//    }
}
