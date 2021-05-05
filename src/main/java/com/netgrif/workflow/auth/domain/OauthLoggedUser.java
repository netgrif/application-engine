package com.netgrif.workflow.auth.domain;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class OauthLoggedUser extends LoggedUser {

    @Getter
    private String oauthId;

    public OauthLoggedUser(Long id, String oauthId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(id, username, password, authorities);
        this.oauthId = oauthId;
    }
}
