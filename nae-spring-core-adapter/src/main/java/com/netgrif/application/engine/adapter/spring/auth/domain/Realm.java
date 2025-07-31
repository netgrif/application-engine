package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.querydsl.core.annotations.QueryEntity;
import org.springframework.data.annotation.Id;

@QueryEntity
public class Realm extends com.netgrif.application.engine.objects.auth.domain.Realm {

    public Realm(String name) {
        super(name);
    }

    @Id
    @Override
    public String getName() {
        return super.getName();
    }
}
