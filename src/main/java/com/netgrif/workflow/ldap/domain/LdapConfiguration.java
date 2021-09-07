//package com.netgrif.workflow.ldap.domain;
//
//
//import com.netgrif.workflow.configuration.properties.NaeLdapProperties;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.ldap.core.LdapTemplate;
//import org.springframework.ldap.core.support.LdapContextSource;
//
//@Configuration
//public class LdapConfiguration {
//
//    @Autowired
//    private NaeLdapProperties ldapProperties;
//
//    @Bean
//    public LdapContextSource contextSource() {
//        LdapContextSource contextSource = new LdapContextSource();
//        contextSource.setUrl(ldapProperties.getUrl());
//        contextSource.setUserDn(ldapProperties.getUsername());
//        contextSource.setPassword(ldapProperties.getPassword());
//        contextSource.setBase(ldapProperties.getBase());
//        return contextSource;
//    }
//
//    @Bean
//    public LdapTemplate ldapTemplate() {
//        return new LdapTemplate(contextSource());
//    }
//
//
//}
