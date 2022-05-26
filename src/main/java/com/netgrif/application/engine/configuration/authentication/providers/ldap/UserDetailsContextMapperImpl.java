package com.netgrif.application.engine.configuration.authentication.providers.ldap;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.ILdapUserRefService;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.ldap.domain.LdapUser;
import com.netgrif.application.engine.ldap.domain.LdapUserRef;
import com.netgrif.application.engine.ldap.service.LdapUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.Collection;

@Slf4j
public class UserDetailsContextMapperImpl implements UserDetailsContextMapper {

    protected LdapUserService ldapUserService;

    protected ILdapUserRefService ldapUserRefService;

    protected NaeLdapProperties properties;

    public UserDetailsContextMapperImpl(LdapUserService ldapUserService, ILdapUserRefService ldapUserRefService, NaeLdapProperties properties) {
        this.ldapUserService = ldapUserService;
        this.ldapUserRefService = ldapUserRefService;
        this.properties = properties;
    }

    @Override
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
            ldapUserRefService.updateById(dirContextOperations.getDn(), user);
        }
        assert user != null;
        return user.transformToLoggedUser();
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        log.error("Community Edition");

    }

}