package com.netgrif.application.engine.startup

import com.netgrif.application.engine.startup.annotation.RunnerOrder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Slf4j
@Component
@RunnerOrder(141)
@ConditionalOnProperty(value = "nae.dashboard-management.enabled", matchIfMissing = true)
class DashboardManagementRunner implements ApplicationEngineStartupRunner {

    @Autowired
    private ImportHelper helper

    public static final String DASHBOARD_MANAGEMENT_NET_IDENTIFIER = "dashboard_management"
    private static final String DASHBOARD_MANAGEMENT_FILE_NAME = "engine-processes/dashboard_management.xml"

    public static final String DASHBOARD_ITEM_NET_IDENTIFIER = "dashboard_item"
    private static final String DASHBOARD_ITEM_FILE_NAME = "engine-processes/dashboard_item.xml"

    @Override
    void run(ApplicationArguments args) throws Exception {
        helper.importProcess("Petri net for dashboard management", DASHBOARD_MANAGEMENT_NET_IDENTIFIER, DASHBOARD_MANAGEMENT_FILE_NAME)
        helper.importProcess("Petri net for dashboard items", DASHBOARD_ITEM_NET_IDENTIFIER, DASHBOARD_ITEM_FILE_NAME)
    }
}
