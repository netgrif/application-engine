package com.netgrif.application.engine.startup

import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Slf4j
@Component
@ConditionalOnProperty(value = "nae.dashboard.enabled", matchIfMissing = false)
class DashboardRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ImportHelper helper

    @Autowired
    private SystemUserRunner systemCreator

    public static final String DASHBOARD_NET_IDENTIFIER = "dashboard"
    private static final String DASHBOARD_FILE_NAME = "engine-processes/dashboard.xml"

    public static final String DASHBOARD_TILE_NET_IDENTIFIER = "dashboard_tile"
    private static final String DASHBOARD_TILE_FILE_NAME = "engine-processes/dashboard_tile.xml"

    @Override
    void run(String... args) throws Exception {
            createDashboardNet()
            createDashboardTileNet()
    }

    Optional<PetriNet> createDashboardNet() {
        importProcess("Petri net for filters", DASHBOARD_NET_IDENTIFIER, DASHBOARD_FILE_NAME)
    }

    Optional<PetriNet> createDashboardTileNet() {
        importProcess("Petri net for filter preferences", DASHBOARD_TILE_NET_IDENTIFIER, DASHBOARD_TILE_FILE_NAME)
    }


    Optional<PetriNet> importProcess(String message, String netIdentifier, String netFileName) {
        PetriNet filter = petriNetService.getNewestVersionByIdentifier(netIdentifier)
        if (filter != null) {
            log.info("${message} has already been imported.")
            return Optional.of(filter)
        }

        Optional<PetriNet> filterNet = helper.createNet(netFileName, VersionType.MAJOR, systemCreator.loggedSystem)

        if (!filterNet.isPresent()) {
            log.error("Import of ${message} failed!")
        }

        return filterNet
    }
}
