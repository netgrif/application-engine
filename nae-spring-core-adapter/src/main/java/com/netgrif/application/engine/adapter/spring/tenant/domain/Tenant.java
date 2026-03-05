package com.netgrif.application.engine.adapter.spring.tenant.domain;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Tenant extends com.netgrif.application.engine.objects.tenant.Tenant {

    public Tenant(String id, String tenantCode) {
        super(id, tenantCode);
    }

    @Id
    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public String getTenantCode() {
        return super.getTenantCode();
    }
}
