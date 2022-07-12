package com.netgrif.application.engine.startup

import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
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

    private static final String FILTER_FILE_NAME = "engine-processes/filter.xml"
    public static final String FILTER_PETRI_NET_IDENTIFIER = "filter"

    private static final String PREFERRED_FILTER_ITEM_FILE_NAME = "engine-processes/preference_filter_item.xml"
    public static final String PREFERRED_FILTER_ITEM_NET_IDENTIFIER = "preference_filter_item"

    private static final String EXPORT_FILTER_FILE_NAME = "engine-processes/export_filters.xml"
    private static final String EXPORT_NET_IDENTIFIER = "export_filters"

    private static final String IMPORT_FILTER_FILE_NAME = "engine-processes/import_filters.xml"
    private static final String IMPORT_NET_IDENTIFIER = "import_filters"

    @Override
    void run(String... args) throws Exception {
        createFilterNet()
        createPreferenceFilterItemNet()
        createImportFiltersNet()
        createExportFiltersNet()
    }

    Optional<PetriNet> createFilterNet() {
        importProcess("Petri net for filters", FILTER_PETRI_NET_IDENTIFIER, FILTER_FILE_NAME)
    }

    Optional<PetriNet> createPreferenceFilterItemNet() {
        importProcess("Petri net for filter preferences", PREFERRED_FILTER_ITEM_NET_IDENTIFIER, PREFERRED_FILTER_ITEM_FILE_NAME)
    }

    Optional<PetriNet> createImportFiltersNet() {
        importProcess("Petri net for importing filters", IMPORT_NET_IDENTIFIER, IMPORT_FILTER_FILE_NAME)
    }

    Optional<PetriNet> createExportFiltersNet() {
        importProcess("Petri net for exporting filters", EXPORT_NET_IDENTIFIER, EXPORT_FILTER_FILE_NAME)
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
