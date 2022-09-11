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

    private static final String IMPERSONATION_CONFIG_USER_SELECT_FILE_NAME = "engine-processes/impersonation_users_select.xml"
    public static final String IMPERSONATION_CONFIG_USER_SELECT_PETRI_NET_IDENTIFIER = "impersonation_users_select"

    @Override
    void run(String... args) throws Exception {
        createConfigNets()
    }

    void createConfigNets() {
        importProcess("Petri net for impersonation config", IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER, IMPERSONATION_CONFIG_FILE_NAME)
        importProcess("Petri net for impersonation user select", IMPERSONATION_CONFIG_USER_SELECT_PETRI_NET_IDENTIFIER, IMPERSONATION_CONFIG_USER_SELECT_FILE_NAME)
    }

    Optional<PetriNet> importProcess(String message, String netIdentifier, String netFileName) {
        PetriNet filter = petriNetService.getNewestVersionByIdentifier(netIdentifier)
        if (filter != null) {
            log.info("${message} has already been imported.")
            return new Optional<>(filter)
        }

        Optional<PetriNet> net = helper.createNet(netFileName, VersionType.MAJOR, systemCreator.loggedSystem)

        if (!net.isPresent()) {
            log.error("Import of ${message} failed!")
        }

        return net
    }
}
