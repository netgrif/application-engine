package com.netgrif.application.engine.configuration.ldap;

import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapConfiguration {

    @Autowired
    private LdapProperties springLdapProperties;

    @Bean
    public LdapContextSource contextSource() {
        String url = springLdapProperties.getUrls() == null || springLdapProperties.getUrls().length == 0 ? "" : springLdapProperties.getUrls()[0];
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setUserDn(springLdapProperties.getUsername());
        contextSource.setPassword(springLdapProperties.getPassword());
        contextSource.setBase(springLdapProperties.getBase());
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }


}
