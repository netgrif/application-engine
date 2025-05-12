package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
public class LoggedUserImpl extends LoggedUser implements UserDetails {

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
