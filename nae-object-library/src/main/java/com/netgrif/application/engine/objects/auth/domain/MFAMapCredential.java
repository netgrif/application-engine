package com.netgrif.application.engine.objects.auth.domain;

import java.util.LinkedHashMap;


public class MFAMapCredential extends Credential<LinkedHashMap<String, Object>> {

    public MFAMapCredential() {
        super();
        this.setType("MFA");
    }

    public MFAMapCredential(String type, LinkedHashMap<String, Object> value, int order, boolean enabled) {
        super("MFA-" + type, value, order, enabled);
    }
}

