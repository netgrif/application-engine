package com.netgrif.application.engine.startup.runner;


import com.netgrif.application.engine.auth.service.TenantService;
import com.netgrif.application.engine.objects.tenant.Tenant;
import com.netgrif.application.engine.objects.tenant.TenantConstants;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.application.engine.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RunnerOrder(48)
@RequiredArgsConstructor
public class AdminTenantRunner implements ApplicationEngineStartupRunner {
    private final TenantService tenantService;
    private final WorkspaceService workspaceService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        Optional<Tenant> tenantOpt = tenantService.getById(TenantConstants.AdminTenant.ID);
        if (tenantOpt.isPresent()) {
            Tenant tenant = tenantOpt.get();
            tenantService.addWorkspace(tenant.getId(), workspaceService.getDefault());
        } else {
            Tenant tenant = createTenant();
            tenantService.save(tenant);
        }
    }

    private Tenant createTenant() {
        Tenant tenant = new com.netgrif.application.engine.adapter.spring.tenant.domain.Tenant(TenantConstants.AdminTenant.ID, TenantConstants.AdminTenant.CODE);
        tenant.setName(TenantConstants.AdminTenant.NAME);
        tenant.addWorkspace(workspaceService.getDefault().getId());
        return tenant;
    }

}
