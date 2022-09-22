package com.netgrif.application.engine.configuration.authentication.providers.ldap;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.ILdapUserRefService;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.ldap.domain.LdapUser;
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
        IUser user = ldapUserService.findByDn(dirContextOperations.getDn());
        if (user == null) {
            LdapUserRef ldapUserOptional = ldapUserRefService.findById(dirContextOperations.getDn());
            if (ldapUserOptional == null) {
                log.warn("Unknown user [" + username + "] tried to log in");
                return null;
            }
            user = ldapUserRefService.createUser(ldapUserOptional);
        } else if (user instanceof LdapUser) {
           user = ldapUserRefService.updateById(dirContextOperations.getDn(), user);
        }
        assert user != null;
        LoggedUser loggedUser = user.transformToLoggedUser();
        if (user instanceof LdapUser && (!((LdapUser) user).getMemberOf().isEmpty())) {
                loggedUser.parseProcessRoles(ldapGroupRefService.getProcessRoleByLdapGroup(((LdapUser) user).getMemberOf()));
            }
        return loggedUser;
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        log.error("Community Edition");

    }

}
