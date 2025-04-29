package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.adapter.spring.auth.domain.mapper.LoggedUserMapper;
import com.netgrif.application.engine.adapter.spring.auth.domain.mapper.UserAuthorMapper;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LoggedUserImpl extends LoggedUser implements UserDetails {

    public LoggedUserImpl(String id, String username, String password, Set<Authority> authorities, Set<ProcessRole> processRoles) {
        super(id, username, password, authorities, processRoles);
    }

    @Builder(builderMethodName = "with")
    public LoggedUserImpl(String id, String realmId, String workspaceId, String createMethod, String username, String email, String password, String firstName, String lastName, Set<Authority> authoritySet, Set<Group> groups, boolean enabled, boolean emailVerified, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, LoggedUser impersonated, Set<ProcessRole> processRoles, Set<String> MFAMethod, Duration sessionTimeout, Map<String,Attribute<?>> attributes) {
        super(id, realmId, workspaceId, createMethod, username, email, password, firstName, lastName, authoritySet, groups, enabled, emailVerified, accountNonExpired, accountNonLocked, credentialsNonExpired, impersonated, processRoles, MFAMethod, sessionTimeout, attributes);
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
    public IUser transformToUser() {
        return LoggedUserMapper.toUser(this);
    }

    @Override
    public Author transformToAuthor() {
        return UserAuthorMapper.toAuthor(LoggedUserMapper.toUser(this));
    }
}
