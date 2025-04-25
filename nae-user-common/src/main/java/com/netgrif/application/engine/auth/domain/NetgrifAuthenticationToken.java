package com.netgrif.application.engine.auth.domain;


import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
public class NetgrifAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String realmId;

    public NetgrifAuthenticationToken(Object principal, Object credentials, String realmId) {
        super(principal, credentials);
        this.realmId = realmId;
    }

}
