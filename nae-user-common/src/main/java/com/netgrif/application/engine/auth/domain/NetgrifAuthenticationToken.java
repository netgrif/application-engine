package com.netgrif.application.engine.auth.domain;


import com.netgrif.application.engine.auth.tenant.TenantClaim;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.workspace.Workspace;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
public class NetgrifAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String realmName;
    private final Realm realm;
    @Setter
    private Workspace workspace;
    @Setter
    private TenantClaim tenantClaim;

    public NetgrifAuthenticationToken(Object principal, Object credentials, String realmName) {
        super(principal, credentials);
        this.realmName = realmName;
        this.realm = null;
    }

    public NetgrifAuthenticationToken(Object principal, Object credentials, Realm realm) {
        super(principal, credentials);
        this.realm = realm;
        this.realmName = realm != null ? realm.getName() : null;
    }

}
