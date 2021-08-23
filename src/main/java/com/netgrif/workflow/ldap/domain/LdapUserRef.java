package com.netgrif.workflow.ldap.domain;


import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.List;


@Entry(
        base = "ou=people",
        objectClasses = {"inetOrgPerson", "person"}
)

@Data
public class LdapUserRef {


    @Id
    private Name dn;


    @Attribute(name = "cn")
    private String cn;


    @Attribute(name = "uid")
    private String uid;


    @Attribute(name = "uid")
    private String mail;


    @Attribute(name = "givenName")
    private String firstName;


    @Attribute(name = "userPassword")
    private byte[] password;


    @Attribute(name = "sn")
    private String surname;


    @Attribute(name = "displayName")
    private String fullName;


    @Attribute(name = "homeDirectory")
    private String homeDirectory;


    @Attribute(name = "objectClass")
    private List<String> objectClass;


    @Attribute(name = "MemberOf")
    private List<String> memberOf;


    @Attribute(name = "pwdAccountLockedTime")
    private String lockingOut;

}