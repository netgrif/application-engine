package com.netgrif.application.engine.objects.auth.domain;

public class StringCredential extends Credential<String> {

    public StringCredential() {
        super();
    }

    public StringCredential(String type, String value, int order, boolean enabled) {
        super(type, value, order, enabled);
    }
}
