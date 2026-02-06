package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.menu.services.interfaces.DashboardManagementService;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.menu.dashboard.DashboardManagementBody;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RunnerOrder(142)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "netgrif.engine.dashboard.enabled", havingValue = "true", matchIfMissing = false)
public class DefaultDashboardRunner implements ApplicationEngineStartupRunner {

    private final DashboardManagementService dashboardManagementService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            createMainDashboardManagementItem();
        } catch (Exception e) {
            log.error("Error while creating default dashboard management item", e);
        }
    }

    private void createMainDashboardManagementItem() throws TransitionNotExecutableException {
        DashboardManagementBody dashboardItemBody = new DashboardManagementBody("main_dashboard", new I18nString("Main Dashboard", Map.of("sk", "Hlavný Dashboard", "de", "Haupt-Dashboard")));
        dashboardItemBody.setLogoutDashboard(true);

        // todo 2072 check if workspaceId is initialized by workflowService.createCase
        dashboardManagementService.createDashboardManagement(dashboardItemBody);
    }
}
