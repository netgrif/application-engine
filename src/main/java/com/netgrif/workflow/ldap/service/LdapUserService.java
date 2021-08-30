package com.netgrif.workflow.ldap.service;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.UserService;
import com.netgrif.workflow.ldap.domain.LdapUser;
import com.netgrif.workflow.ldap.domain.repository.LdapUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import javax.naming.Name;

@Slf4j
@ConditionalOnExpression("${nae.ldap.enabled}")
public class LdapUserService extends UserService {

    @Autowired
    private LdapUserRepository ldapUserRepository;

    public LdapUser findByDn(Name dn) {
        return ldapUserRepository.findByDn(dn.toString());
    }


    protected LdapUser getUserFromLdap(User user) {
        if (user instanceof LdapUser) {
            return (LdapUser) user;
        } else {
            return transformToUserFromLdap(user);
        }
    }


    public LdapUser transformToUserFromLdap(User user) {

        LdapUser userFromLdap = ldapUserRepository.findByEmail(user.getEmail());
        if (userFromLdap == null && user.getId() != null) {
            userFromLdap = new LdapUser(user.getId());
        } else if (userFromLdap == null) {
            userFromLdap = new LdapUser();
        }
        userFromLdap.loadFromUser(user);
        return userFromLdap;
    }

}