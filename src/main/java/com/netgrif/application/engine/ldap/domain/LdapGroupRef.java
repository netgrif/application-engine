package com.netgrif.application.engine.ldap.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

@Data
@AllArgsConstructor
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapGroupRef {

    @Id
    private Name dn;

    private String cn;

    private String[] member;

    private String[] objectClass;

    private String description;

}
