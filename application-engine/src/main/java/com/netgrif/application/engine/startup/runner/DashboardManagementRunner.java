package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(141)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "netgrif.engine.dashboard.enabled", havingValue = "true", matchIfMissing = false)
public class DashboardManagementRunner implements ApplicationEngineStartupRunner {

    private final ImportHelper helper;

    public static final String DASHBOARD_MANAGEMENT_NET_IDENTIFIER = "dashboard_management";
    private static final String DASHBOARD_MANAGEMENT_FILE_NAME = "engine-processes/dashboard_management.xml";

    public static final String DASHBOARD_ITEM_NET_IDENTIFIER = "dashboard_item";
    private static final String DASHBOARD_ITEM_FILE_NAME = "engine-processes/dashboard_item.xml";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        helper.importProcessOnce("Petri net for dashboard management", DASHBOARD_MANAGEMENT_NET_IDENTIFIER, DASHBOARD_MANAGEMENT_FILE_NAME);
        helper.importProcessOnce("Petri net for dashboard items", DASHBOARD_ITEM_NET_IDENTIFIER, DASHBOARD_ITEM_FILE_NAME);
    }
}
