package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Attribute;
import com.netgrif.application.engine.objects.tenant.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.netgrif.application.engine.objects.tenant.TenantConstants.TENANT_EMPTY;
import static com.netgrif.application.engine.objects.tenant.TenantConstants.ACTIVE_TENANT_ID;

@Component
@RequiredArgsConstructor
public class TenantUserServiceImpl implements TenantUserService {

    private final TenantService tenantService;
    private final UserService userService;
    private final RealmService realmService;

    @Override
    public String getLoggedUserActiveTenantId() {
        AbstractUser user = userService.getLoggedUser();
        Attribute<?> tenantAtt = user.getAttribute(ACTIVE_TENANT_ID);
        if (tenantAtt == null || tenantAtt.hasNullValue()) {
            return null;
        }
        return (String) tenantAtt.getValue();
    }

    @Override
    public String getAndUpdateLoggedUserWithActiveTenantId() {
        AbstractUser user = userService.getLoggedUser();
        return getAndUpdateUserWithActiveTenantId(user);

    }

    @Override
    public String getAndUpdateUserWithActiveTenantId(AbstractUser user) {
        return realmService
                .getRealmById(user.getRealmId())
                .flatMap(realm -> tenantService.getByRealm(user.getRealmId()))
                .map(tenant -> {
                    TenantUserService.addActiveTenantIdToUser(user, tenant.getId());
                    return tenant.getId();
                }).orElse(null);

    }

    @Override
    public void addActiveTenantIdToUser(AbstractUser user) {
        Optional<Tenant> tenant = tenantService.getByRealm(user.getRealmId());
        String tenantId = tenant.map(Tenant::getId).orElse(TENANT_EMPTY);
        TenantUserService.addActiveTenantIdToUser(user, tenantId);
    }

    @Override
    public void addAndUpdateActiveTenantIdToUser(AbstractUser user) {
        addActiveTenantIdToUser(user);
        userService.saveUser(user);
    }


}
