package com.netgrif.application.engine.configuration.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@ConditionalOnExpression("${nae.ldap.enabled:false} AND ${nae.ldap.ignore-partial:false}")
public class AdLdapConfiguration {

    @Autowired
    protected LdapContextSource ldapContextSource;

    @Bean
    @Primary
    public LdapTemplate ldapTemplate() {
        LdapTemplate template = new LdapTemplate(ldapContextSource);
        template.setIgnorePartialResultException(true);
        return template;
    }

}
