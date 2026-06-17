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
@RunnerOrder(140)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "netgrif.engine.dashboard.enabled", havingValue = "true", matchIfMissing = false)
public class DashboardRunner implements ApplicationEngineStartupRunner {

    private final ImportHelper helper;

    public static final String DASHBOARD_NET_IDENTIFIER = "dashboard";
    private static final String DASHBOARD_FILE_NAME = "engine-processes/dashboard.xml";

    public static final String DASHBOARD_TILE_NET_IDENTIFIER = "dashboard_tile";
    private static final String DASHBOARD_TILE_FILE_NAME = "engine-processes/dashboard_tile.xml";


    @Override
    public void run(ApplicationArguments args) throws Exception {
        helper.importProcessOnce("Petri net for dashboard", DASHBOARD_NET_IDENTIFIER, DASHBOARD_FILE_NAME);
        helper.importProcessOnce("Petri net for dashboard tile", DASHBOARD_TILE_NET_IDENTIFIER, DASHBOARD_TILE_FILE_NAME);
    }
}
