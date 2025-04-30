package com.netgrif.application.engine.objects.auth.domain;

public class PasswordCredential extends Credential<String> {

    public PasswordCredential() {
        super();
        this.setType("password");
    }

    public PasswordCredential(String value, int order, boolean enabled) {
        super("password", value, order, enabled);
    }
}
