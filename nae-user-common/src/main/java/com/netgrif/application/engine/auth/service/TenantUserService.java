package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Attribute;

import java.util.ArrayList;
import java.util.List;

import static com.netgrif.application.engine.objects.tenant.TenantConstants.ACTIVE_TENANT_ID;
import static com.netgrif.application.engine.objects.tenant.TenantConstants.TENANTS;

public interface TenantUserService {
    String getLoggedUserActiveTenantId();

    String getAndUpdateLoggedUserWithActiveTenantId();

    String getAndUpdateUserWithActiveTenantId(AbstractUser user);

    void addActiveTenantIdToUser(AbstractUser user);

    void addAndUpdateActiveTenantIdToUser(AbstractUser user);

    static void addActiveTenantIdToUser(AbstractUser user, String tenantId) {
        user.setAttribute(ACTIVE_TENANT_ID, tenantId, false);
    }

    static void setTenantsToUser(AbstractUser user, String... tenantIds) {
        List<String> tenantIdsList = new ArrayList<>(List.of(tenantIds));
        user.setAttribute(TENANTS, tenantIdsList, false);
    }

    static void addTenantsToUser(AbstractUser user, String... tenantIds) {
        Attribute<?> tenantAtt = user.getAttribute(TENANTS);
        if (tenantAtt == null || tenantAtt.hasNullValue()) {
            setTenantsToUser(user, tenantIds);
            return;
        }
        @SuppressWarnings("unchecked")
        List<String> tenants = (List<String>) tenantAtt.getValue();
        tenants.addAll(List.of(tenantIds));
    }

    static void addTenantId(AbstractUser user, String tenantId) {
        Attribute<?> tenantAtt = user.getAttribute(TENANTS);
        if (tenantAtt.hasNullValue()) {
            List<String> tenants = new ArrayList<>();
            tenants.add(tenantId);
            user.setAttribute(TENANTS, tenants, false);
            return;
        }
        @SuppressWarnings("unchecked")
        List<String> tenants = (List<String>) tenantAtt.getValue();
        tenants.add(tenantId);
        user.setAttribute(TENANTS, tenants, false);
    }
}
