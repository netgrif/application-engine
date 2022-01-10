package com.netgrif.workflow.ldap.service.interfaces;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.ldap.domain.LdapUserRef;

import javax.naming.Name;


public interface ILdapUserRefService {


    User createUser(LdapUserRef ldapUser);


    User updateById(Name id, User savedUser);

    LdapUserRef findById(Name id);

    LdapUserRef findUserByDn(String dn);


    LdapUserRef findUserByCn(String cn);

}