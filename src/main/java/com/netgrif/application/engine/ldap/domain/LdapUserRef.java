package com.netgrif.application.engine.ldap.domain;


import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.List;
import java.util.Set;


@Data
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapUserRef {

    @Id
    private Name dn;

    private String cn;

    private String uid;

    private String mail;

    private String firstName;

    private byte[] password;

    private String surname;

    private String fullName;

    private String telNumber;

    private String homeDirectory;

    private List<String> objectClass;

    private Set<String> memberOf;

    private String lockingOut;

}
