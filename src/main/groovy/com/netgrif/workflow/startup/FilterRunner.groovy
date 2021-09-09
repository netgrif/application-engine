package com.netgrif.workflow.startup

import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class FilterRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ImportHelper helper

    @Autowired
    private SystemUserRunner systemCreator

    private static final String FILTER_FILE_NAME = "engine-processes/filter.xml";
    private static final String FILTER_PETRI_NET_IDENTIFIER = "filter"

    @Override
    void run(String... args) throws Exception {
        importFilterProcess()
    }

    ImportPetriNetEventOutcome importFilterProcess() {
        PetriNet filter = petriNetService.getNewestVersionByIdentifier(FILTER_PETRI_NET_IDENTIFIER)
        if (filter != null) {
            log.info("Petri net for filters has already been imported.")
            return new ImportPetriNetEventOutcome()
        }

        ImportPetriNetEventOutcome filterNet = helper.createNet(FILTER_FILE_NAME, VersionType.MAJOR, systemCreator.loggedSystem)

        if (filterNet.getNet() == null) {
            log.error("Import of Petri net for filters failed!")
        }

        return filterNet
    }
}
