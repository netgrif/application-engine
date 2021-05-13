package com.netgrif.workflow.oauth.domain;

import com.netgrif.workflow.auth.domain.LoggedUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class OAuthLoggedUser extends LoggedUser {

    @Getter
    protected String oauthId;

    public OAuthLoggedUser(String id, String oauthId, Collection<? extends GrantedAuthority> authorities) {
        super(id, oauthId, "n/a", authorities);
        this.oauthId = oauthId;
    }
}
