package com.netgrif.application.engine.ldap.service;


import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.ldap.domain.LdapUser;
import com.netgrif.application.engine.ldap.domain.repository.LdapUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.naming.Name;

@Slf4j
@Service
@Primary
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapUserService extends UserService {

    @Autowired
    private LdapUserRepository ldapUserRepository;

    @Autowired
    private LdapGroupRefService ldapGroupRefService;

    public LdapUser findByDn(Name dn) {
        return ldapUserRepository.findByDn(dn.toString());
    }

    @Override
    public IUser findByEmail(String email, boolean small) {
        IUser user = userRepository.findByEmail(email);
        if (user instanceof LdapUser && (((LdapUser) user).getMemberOf() != null && !(((LdapUser) user).getMemberOf().isEmpty()))) {
            ldapGroupRefService.getProcessRoleByLdapGroup(((LdapUser) user).getMemberOf()).forEach(user::addProcessRole);
        }
        return user;
    }

    protected LdapUser getUserFromLdap(IUser user) {
        if (user instanceof LdapUser) {
            return (LdapUser) user;
        } else {
            return transformToUserFromLdap(user);
        }
    }


    public LdapUser transformToUserFromLdap(IUser user) {

        LdapUser userFromLdap = ldapUserRepository.findByEmail(user.getEmail());
        if (userFromLdap == null && user.getStringId() != null) {
            userFromLdap = new LdapUser(new ObjectId(user.getStringId()));
        } else if (userFromLdap == null) {
            userFromLdap = new LdapUser();
        }
        userFromLdap.loadFromUser(user);
        return userFromLdap;
    }

}
