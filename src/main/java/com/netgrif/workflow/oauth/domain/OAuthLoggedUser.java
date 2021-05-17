package com.netgrif.workflow.oauth.domain;

import com.netgrif.workflow.auth.domain.LoggedUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class OAuthLoggedUser extends LoggedUser {

    @Getter
    protected String dbId;

    public OAuthLoggedUser(String oauthId, String dbId, String username, Collection<? extends GrantedAuthority> authorities) {
        super(oauthId, username, "n/a", authorities);
        this.dbId = dbId;
    }
}
