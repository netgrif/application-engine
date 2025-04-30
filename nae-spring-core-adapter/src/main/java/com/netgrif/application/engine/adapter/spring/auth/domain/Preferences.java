package com.netgrif.application.engine.adapter.spring.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Preferences extends com.netgrif.application.engine.objects.auth.domain.Preferences {

    public Preferences(String userId) {
        super(userId);
    }

    @Id
    @Override
    public String getUserId() {
        return super.getUserId();
    }
}
