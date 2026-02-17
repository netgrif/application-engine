package com.netgrif.application.engine.adapter.spring.tenant.domain;

import com.netgrif.application.engine.objects.tenant.Tenant;
import org.springframework.stereotype.Component;

//todo maybe as properties
public final class AdminTenant extends Tenant {

    public static final String ADMIN_TENANT_ID = "netgrif";

    public static final String ADMIN_TENANT_CODE = "netgrif";

    public static final String ADMIN_TENANT_NAME = "Netgrif Admin";

    public AdminTenant() {
        super(ADMIN_TENANT_ID, ADMIN_TENANT_CODE);
        setName(ADMIN_TENANT_NAME);
    }

    public boolean equals(Tenant other) {
        return other instanceof AdminTenant;
    }

}
