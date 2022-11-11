package com.netgrif.application.engine.configuration.authentication.tokens;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public class NetgrifAnonymousAuthenticationToken  extends AbstractAuthenticationToken {

    private final Object principal;

    private final int keyHash;

    public NetgrifAnonymousAuthenticationToken(String key, Object principal,
                                        Collection<? extends GrantedAuthority> authorities) {
        this(extractKeyHash(key), principal, authorities);
    }

    private NetgrifAnonymousAuthenticationToken(Integer keyHash, Object principal,
                                         Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        Assert.isTrue(principal != null && !"".equals(principal), "principal cannot be null or empty");
        Assert.notEmpty(authorities, "authorities cannot be null or empty");
        this.keyHash = keyHash;
        this.principal = principal;
        setAuthenticated(true);
    }

    private static Integer extractKeyHash(String key) {
        Assert.hasLength(key, "key cannot be empty or null");
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof NetgrifAnonymousAuthenticationToken) {
            NetgrifAnonymousAuthenticationToken test = (NetgrifAnonymousAuthenticationToken) obj;
            return (this.getKeyHash() == test.getKeyHash());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.keyHash;
        return result;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    public int getKeyHash() {
        return this.keyHash;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
