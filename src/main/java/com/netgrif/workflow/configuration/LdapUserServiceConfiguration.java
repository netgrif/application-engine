package com.netgrif.workflow.configuration;

import com.netgrif.workflow.auth.service.LdapUserDetailsService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.ldap.service.LdapUserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@ConditionalOnExpression("${nae.ldap.enabled}")
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