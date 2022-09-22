package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.auth.service.LdapUserDetailsService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.ldap.service.LdapUserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapUserServiceConfiguration {

    @Bean
    public IUserService userService() {
        return new LdapUserService();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new LdapUserDetailsService();
    }

}
