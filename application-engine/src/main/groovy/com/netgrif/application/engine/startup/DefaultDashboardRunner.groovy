package com.netgrif.application.engine.startup


import com.netgrif.application.engine.menu.services.interfaces.DashboardManagementService
import com.netgrif.application.engine.objects.petrinet.domain.I18nString
import com.netgrif.application.engine.objects.workflow.domain.menu.dashboard.DashboardManagementBody
import com.netgrif.application.engine.startup.annotation.RunnerOrder
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.stereotype.Component

@Slf4j
@Component
@RunnerOrder(102)
@RequiredArgsConstructor
class DefaultDashboardRunner implements ApplicationEngineStartupRunner {

    @Autowired
    private DashboardManagementService dashboardManagementService

    @Override
    void run(ApplicationArguments args) throws Exception {
        createMainDashboardManagementItem()
    }

    def createMainDashboardManagementItem() {
        def dashboardItemBody = new DashboardManagementBody("main_dashboard", new I18nString("Main Dashboard",Map.of("sk","Hlavný Dashboard","de","Haupt-Dashboard")))
        dashboardItemBody.setLogoutDashboard(true)

        return dashboardManagementService.createDashboardManagement(dashboardItemBody)
    }
}
