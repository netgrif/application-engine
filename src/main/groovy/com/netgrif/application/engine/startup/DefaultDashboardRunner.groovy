package com.netgrif.application.engine.startup


import com.netgrif.application.engine.menu.domain.dashboard.DashboardManagementBody
import com.netgrif.application.engine.menu.services.interfaces.DashboardManagementService
import com.netgrif.application.engine.petrinet.domain.I18nString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DefaultDashboardRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private DashboardManagementService dashboardManagementService

    @Override
    void run(String... args) throws Exception {
        createMainDashboardManagementItem()
    }

    def createMainDashboardManagementItem() {
        def dashboardItemBody = new DashboardManagementBody("main_dashboard", new I18nString("Main Dashboard",Map.of("sk","Hlavný Dashboard","de","Haupt-Dashboard")))
        dashboardItemBody.setLogoutDashboard(true)

        return dashboardManagementService.createDashboardManagement(dashboardItemBody)
    }
}
