package com.netgrif.application.engine.objects.auth.domain;

public class MFAStringCredential extends Credential<String> {

    public MFAStringCredential() {
        super();
        this.setType("MFA");
    }

    public MFAStringCredential(String type, String value, int order, boolean enabled) {
        super("MFA-" + type, value, order, enabled);
    }
}
