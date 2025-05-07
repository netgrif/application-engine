package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class LoggedUserImpl extends LoggedUser implements UserDetails {

    public LoggedUserImpl(String id, String username, String password, Set<Authority> authorities, Set<ProcessRole> processRoles, Set<ProcessRole> negativeProcessRoles) {
        super(id, username, password, authorities, processRoles, negativeProcessRoles);
    }

    @Builder(builderMethodName = "with")
    public LoggedUserImpl(String id, String realmId, String createMethod, String username, String email, String password, String firstName, String lastName, Set<Authority> authoritySet, Set<Group> groups, boolean enabled, boolean emailVerified, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, LoggedUser impersonated, Set<ProcessRole> processRoles, Set<String> MFAMethod, Duration sessionTimeout, Map<String,Attribute<?>> attributes, Set<ProcessRole> negativeProcessRoles) {
        super(id, realmId, createMethod, username, email, password, firstName, lastName, authoritySet, groups, enabled, emailVerified, accountNonExpired, accountNonLocked, credentialsNonExpired, impersonated, processRoles, negativeProcessRoles, MFAMethod, sessionTimeout, attributes);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthoritySet().stream().map(authority -> {
            if (authority instanceof AuthorityImpl) {
                return (AuthorityImpl) authority;
            }
            return new AuthorityImpl(authority);
        }).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return "N/A";
    }
}
