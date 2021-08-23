package com.netgrif.workflow.ldap.service;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.UserService;
import com.netgrif.workflow.ldap.domain.LdapUser;
import com.netgrif.workflow.ldap.domain.repository.LdapUserRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.naming.Name;


@Service

@Primary

@Slf4j

public class LdapUserService extends UserService {


    @Autowired
    private LdapUserRepository ldapUserRepository;


    @Autowired
    private ProcessRoleRepository processRoleRepository;



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