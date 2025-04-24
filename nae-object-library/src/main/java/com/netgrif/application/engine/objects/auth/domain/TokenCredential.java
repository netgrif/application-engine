package com.netgrif.application.engine.objects.auth.domain;

public class TokenCredential extends Credential<String> {

    public TokenCredential() {
        super();
        this.setType("token");
    }

    public TokenCredential(String value, int order, boolean enabled) {
        super("token", value, order, enabled);
    }
}
