package com.netgrif.workflow.ldap.service.interfaces;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.ldap.domain.LdapUserRef;


public interface ILdapUserRefService {


    User createUser(LdapUserRef ldapUser);


    LdapUserRef findUserByDn(String dn);


    LdapUserRef findUserByCn(String cn);

}