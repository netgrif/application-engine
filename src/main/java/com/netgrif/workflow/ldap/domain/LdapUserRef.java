package com.netgrif.workflow.ldap.domain;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.List;


@Entry(
        objectClasses = {"inetOrgPerson", "person"}
)

@Data
public class LdapUserRef {


    @Id
    private Name dn;

    @Value("${nae.ldap.mapCn}")
//    @Attribute()
    private String cn;


    @Attribute(name = "${nae.ldap.uid}")
    private String uid;


    @Attribute(name = "${nae.ldap.mapMail}")
    private String mail;


    @Attribute(name = "${nae.ldap.mapFirstName}")
    private String firstName;


    @Attribute(name = "${nae.ldap.mapUserPassword}")
    private byte[] password;


    @Attribute(name = "${nae.ldap.mapSurname}")
    private String surname;


    @Attribute(name = "${nae.ldap.mapDisplayName}")
    private String fullName;


    @Attribute(name = "${nae.ldap.mapHomeDirectory}")
    private String homeDirectory;


    @Attribute(name = "${nae.ldap.mapObjectClass}")
    private List<String> objectClass;


    @Attribute(name = "${nae.ldap.mapMemberOf}")
    private List<String> memberOf;


    @Attribute(name = "pwdAccountLockedTime")
    private String lockingOut;

}