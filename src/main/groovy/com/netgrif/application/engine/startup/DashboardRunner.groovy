package com.netgrif.application.engine.startup

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Slf4j
@Component
@ConditionalOnProperty(value = "nae.dashboard.enabled", matchIfMissing = false)
class DashboardRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ImportHelper helper

    public static final String DASHBOARD_NET_IDENTIFIER = "dashboard"
    private static final String DASHBOARD_FILE_NAME = "engine-processes/dashboard.xml"

    public static final String DASHBOARD_TILE_NET_IDENTIFIER = "dashboard_tile"
    private static final String DASHBOARD_TILE_FILE_NAME = "engine-processes/dashboard_tile.xml"

    @Override
    void run(String... args) throws Exception {
        helper.importProcess("Petri net for filters", DASHBOARD_NET_IDENTIFIER, DASHBOARD_FILE_NAME)
        helper.importProcess("Petri net for filter preferences", DASHBOARD_TILE_NET_IDENTIFIER, DASHBOARD_TILE_FILE_NAME)
    }
}
