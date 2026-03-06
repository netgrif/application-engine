package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.tenant.TenantConstants;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RunnerOrder(49)
@ConditionalOnProperty(value = "realm.create-default", matchIfMissing = true)
public class DefaultRealmRunner implements ApplicationEngineStartupRunner {

    private final RealmService realmService;

    public DefaultRealmRunner(RealmService realmService) {
        this.realmService = realmService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (realmService.getDefaultRealm(TenantConstants.AdminTenant.ID).isPresent()) {
            return;
        }

        Realm createRequest = new com.netgrif.application.engine.adapter.spring.auth.domain.Realm("Default");
        createRequest.setTenantId(TenantConstants.AdminTenant.ID);
        createRequest.setDescription("Default realm");
        createRequest.setAdminRealm(true);
        createRequest.setDefaultRealm(true);
        realmService.createRealm(createRequest);
    }
}
