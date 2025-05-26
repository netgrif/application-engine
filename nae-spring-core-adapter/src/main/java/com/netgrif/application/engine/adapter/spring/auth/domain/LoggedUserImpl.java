package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class LoggedUserImpl extends LoggedUser implements UserDetails {

    public LoggedUserImpl(ObjectId id, String realmId, String username, String firstName, String middleName, String lastName, String email, String avatar, String workspaceId, String providerOrigin, Set<String> mfaMethods, Duration sessionTimeout) {
        super(id, realmId, username, firstName, middleName, lastName, email, avatar, workspaceId, providerOrigin, mfaMethods, sessionTimeout);
    }

    public LoggedUserImpl(String id, String realmId, String username, String firstName, String middleName, String lastName, String email, String avatar, String workspaceId, String providerOrigin, Set<String> mfaMethods, Duration sessionTimeout) {
        super(id, realmId, username, firstName, middleName, lastName, email, avatar, workspaceId, providerOrigin, mfaMethods, sessionTimeout);
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

    @Override
    public void setPassword(String password) {
    }
}
