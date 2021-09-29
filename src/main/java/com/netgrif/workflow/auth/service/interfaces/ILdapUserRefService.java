package com.netgrif.workflow.auth.service.interfaces;


import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.ldap.domain.LdapUserRef;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import javax.naming.Name;

@ConditionalOnExpression("${nae.ldap.enabled}")
public interface ILdapUserRefService {


    IUser createUser(LdapUserRef ldapUser);

    IUser updateById(Name id, IUser savedUser);

    LdapUserRef findById(Name id);

    LdapUserRef findUserByDn(String dn);

    LdapUserRef findUserByCn(String cn);

}