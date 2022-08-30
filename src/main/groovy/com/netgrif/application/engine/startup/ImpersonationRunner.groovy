package com.netgrif.application.engine.startup

import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class ImpersonationRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ImportHelper helper

    @Autowired
    private SystemUserRunner systemCreator

    private static final String IMPERSONATION_CONFIG_FILE_NAME = "engine-processes/impersonation_config.xml"
    public static final String IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER = "impersonation_config"

    @Override
    void run(String... args) throws Exception {
        createConfigNet()
    }

    Optional<PetriNet> createConfigNet() {
        importProcess("Petri net for filters", IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER, IMPERSONATION_CONFIG_FILE_NAME)
    }

    Optional<PetriNet> importProcess(String message, String netIdentifier, String netFileName) {
        PetriNet filter = petriNetService.getNewestVersionByIdentifier(netIdentifier)
        if (filter != null) {
            log.info("${message} has already been imported.")
            return new Optional<>(filter)
        }

        Optional<PetriNet> filterNet = helper.createNet(netFileName, VersionType.MAJOR, systemCreator.loggedSystem)

        if (!filterNet.isPresent()) {
            log.error("Import of ${message} failed!")
        }

        return filterNet
    }
}
