package com.netgrif.application.engine.configuration.authentication.providers.ldap;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.service.interfaces.ILdapUserRefService;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.ldap.domain.LdapUserRef;
import com.netgrif.application.engine.ldap.service.LdapUserService;
import com.netgrif.application.engine.ldap.service.interfaces.ILdapGroupRefService;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.Collection;

@Slf4j
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class UserDetailsContextMapperImpl implements UserDetailsContextMapper {
    protected LdapUserService ldapUserService;

    protected ILdapUserRefService ldapUserRefService;

    protected NaeLdapProperties properties;

    protected ILdapGroupRefService ldapGroupRefService;


    public UserDetailsContextMapperImpl(LdapUserService ldapUserService, ILdapUserRefService ldapUserRefService, ILdapGroupRefService ldapGroupRefService, NaeLdapProperties properties) {
        this.ldapUserService = ldapUserService;
        this.ldapUserRefService = ldapUserRefService;
        this.ldapGroupRefService = ldapGroupRefService;
        this.properties = properties;
    }

    @Override
    @Synchronized
    public UserDetails mapUserFromContext(DirContextOperations dirContextOperations, String username, Collection<? extends GrantedAuthority> authorities) {
        dirContextOperations.setAttributeValues("objectClass", properties.getPeopleClass());
        Identity identity = ldapUserService.findByDn(dirContextOperations.getDn());
        if (identity == null) {
            LdapUserRef ldapUserOptional = ldapUserRefService.findById(dirContextOperations.getDn());
            if (ldapUserOptional == null) {
                log.warn("Unknown user [{}] tried to log in", username);
                return null;
            }
//            identity = ldapUserRefService.createUser(ldapUserOptional);
        } else if (true/*user instanceof LdapUser*/) {
//            identity = ldapUserRefService.updateById(dirContextOperations.getDn(), user);
        }
        assert identity != null;
        LoggedIdentity loggedIdentity = identity.toSession();
//        if (user instanceof LdapUser && (!((LdapUser) user).getMemberOf().isEmpty())) {
            // todo: release/8.0.0
//                loggedUser.addRoleAssignments(ldapGroupRefService.getRoleByLdapGroup(((LdapUser) user).getMemberOf()));
//            }
        return loggedIdentity;
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        log.error("Community Edition");

    }

}
