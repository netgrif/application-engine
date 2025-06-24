package com.netgrif.application.engine.auth.domain;


import com.netgrif.application.engine.objects.auth.domain.Realm;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
public class NetgrifAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String realmId;
    private final Realm realm;

    public NetgrifAuthenticationToken(Object principal, Object credentials, String realmId) {
        super(principal, credentials);
        this.realmId = realmId;
        this.realm = null;
    }

    public NetgrifAuthenticationToken(Object principal, Object credentials, Realm realm) {
        super(principal, credentials);
        this.realm = realm;
        this.realmId = realm != null ? realm.getId() : null;
    }

}
