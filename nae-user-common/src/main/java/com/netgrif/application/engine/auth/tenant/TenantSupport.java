package com.netgrif.application.engine.auth.tenant;

import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.auth.service.TenantService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Attribute;
import com.netgrif.application.engine.objects.tenant.Tenant;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Optional;

import static com.netgrif.application.engine.objects.tenant.TenantConstants.TENANT_EMPTY;
import static com.netgrif.application.engine.objects.tenant.TenantConstants.TENANT_ID;


public class TenantSupport implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TenantSupport.applicationContext = applicationContext;
    }

    private static TenantService getTenantService() {
        return applicationContext.getBean(TenantService.class);
    }

    private static UserService getUserService() {
        return applicationContext.getBean(UserService.class);
    }

    private static RealmService getRealmService() {
        return applicationContext.getBean(RealmService.class);
    }

    public static String getLoggedUserTenantId() {
        AbstractUser user = getUserService().getLoggedUser();
        Attribute<?> tenantAtt = user.getAttribute(TENANT_ID);
        if (tenantAtt == null || tenantAtt.hasNullValue()) {
            return null;
        }
        return (String) tenantAtt.getValue();
    }

    public static String getAndUpdateLoggedUserWithTenantId() {
        AbstractUser user = getUserService().getLoggedUser();
        return getAndUpdateUserWithTenantId(user);

    }

    public static String getAndUpdateUserWithTenantId(AbstractUser user) {
        return getRealmService()
                .getRealmById(user.getRealmId())
                .flatMap(realm -> getTenantService().getByRealm(user.getRealmId()))
                .map(tenant -> {
                    decorateUserWithTenant(user, tenant.getId());
                    return tenant.getId();
                }).orElse(null);

    }

    public static void decorateUserWithTenant(AbstractUser user) {
        Optional<Tenant> tenant = getTenantService().getByRealm(user.getRealmId());
        String tenantId = tenant.map(Tenant::getId).orElse(TENANT_EMPTY);
        decorateUserWithTenant(user, tenantId);
    }

    public static void decorateUserWithTenant(AbstractUser user, String tenantId) {
        user.setAttribute(TENANT_ID, tenantId, false);
    }

    public static void decorateAndUpdateUserWithTenant(AbstractUser user) {
        decorateUserWithTenant(user);
        getUserService().saveUser(user);
    }


}
