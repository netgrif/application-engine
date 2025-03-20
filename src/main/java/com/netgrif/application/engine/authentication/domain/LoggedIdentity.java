package com.netgrif.application.engine.authentication.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * todo javadoc
 * */
@Getter
public class LoggedIdentity extends org.springframework.security.core.userdetails.User {
    protected final String identityId;
    @Setter
    protected String activeActorId;

    @Builder
    public LoggedIdentity(String identityId, String activeActorId, String username, String password,
                          Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.identityId = identityId;
        this.activeActorId = activeActorId;
    }
}
