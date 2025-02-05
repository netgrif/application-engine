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
@ConditionalOnProperty(value = "nae.dashboard-management.enabled", matchIfMissing = true)
class DashboardManagementRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ImportHelper helper

    @Autowired
    private SystemUserRunner systemCreator

    public static final String DASHBOARD_MANAGEMENT_NET_IDENTIFIER = "dashboard_management"
    private static final String DASHBOARD_MANAGEMENT_FILE_NAME = "engine-processes/dashboard_management.xml"

    public static final String DASHBOARD_ITEM_NET_IDENTIFIER = "dashboard_item"
    private static final String DASHBOARD_ITEM_FILE_NAME = "engine-processes/dashboard_item.xml"

    @Override
    void run(String... args) throws Exception {
        createDashboardManagementNet()
        createDashboardItemNet()
    }

    Optional<PetriNet> createDashboardManagementNet() {
        importProcess("Petri net for filters", DASHBOARD_MANAGEMENT_NET_IDENTIFIER, DASHBOARD_MANAGEMENT_FILE_NAME)
    }

    Optional<PetriNet> createDashboardItemNet() {
        importProcess("Petri net for filter preferences", DASHBOARD_ITEM_NET_IDENTIFIER, DASHBOARD_ITEM_FILE_NAME)
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
