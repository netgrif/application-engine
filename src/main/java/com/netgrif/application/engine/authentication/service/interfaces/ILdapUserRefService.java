package com.netgrif.application.engine.authentication.service.interfaces;


import com.netgrif.application.engine.ldap.domain.LdapUserRef;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import javax.naming.Name;

@ConditionalOnExpression("${nae.ldap.enabled:false}")
public interface ILdapUserRefService {

    IUser createUser(LdapUserRef ldapUser);

    IUser updateById(Name id, IUser savedUser);

    LdapUserRef findById(Name id);

    LdapUserRef findUserByDn(String dn);

    LdapUserRef findUserByCn(String cn);

}
