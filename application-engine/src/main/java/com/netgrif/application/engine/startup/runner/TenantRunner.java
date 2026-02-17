package com.netgrif.application.engine.startup.runner;


import com.netgrif.application.engine.adapter.spring.tenant.domain.AdminTenant;
import com.netgrif.application.engine.adapter.spring.tenant.service.TenantService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.application.engine.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(51)
@RequiredArgsConstructor
public class TenantRunner implements ApplicationEngineStartupRunner {
    private final TenantService tenantService;
    private final WorkspaceService workspaceService;
    private final AdminTenant adminTenant;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        tenantService.addWorkspace(adminTenant.getId(), workspaceService.getDefault().getId());
    }
}
