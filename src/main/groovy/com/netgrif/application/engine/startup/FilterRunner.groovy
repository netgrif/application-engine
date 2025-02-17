package com.netgrif.application.engine.startup

import com.netgrif.application.engine.menu.registry.interfaces.IMenuItemViewRegistry
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class FilterRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ImportHelper helper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SystemUserRunner systemCreator

    @Autowired
    private IMenuItemViewRegistry viewRegistry

    private static final String FILTER_FILE_NAME = "engine-processes/filter.xml"
    public static final String FILTER_PETRI_NET_IDENTIFIER = "filter"

    private static final String MENU_ITEM_FILE_NAME = "engine-processes/menu/menu_item.xml"
    public static final String MENU_NET_IDENTIFIER = "menu_item"

    private static final String EXPORT_FILTER_FILE_NAME = "engine-processes/export_filters.xml"
    private static final String EXPORT_NET_IDENTIFIER = "export_filters"

    private static final String IMPORT_FILTER_FILE_NAME = "engine-processes/import_filters.xml"
    private static final String IMPORT_NET_IDENTIFIER = "import_filters"

    @Override
    void run(String... args) throws Exception {
        helper.importProcess("Petri net for filters", FILTER_PETRI_NET_IDENTIFIER, FILTER_FILE_NAME)
        createConfigurationNets()
        helper.importProcess("Petri net for filter preferences", MENU_NET_IDENTIFIER, MENU_ITEM_FILE_NAME)
        helper.importProcess("Petri net for importing filters", IMPORT_NET_IDENTIFIER, IMPORT_FILTER_FILE_NAME)
        helper.importProcess("Petri net for exporting filters", EXPORT_NET_IDENTIFIER, EXPORT_FILTER_FILE_NAME)
    }

    private List<PetriNet> createConfigurationNets() {
        return viewRegistry.getAllViews().each { viewEntry ->
            String processIdentifier = viewEntry.getKey() + "_configuration"
            String filePath = String.format("engine-processes/menu/%s.xml", processIdentifier)
            helper.importProcess(String.format("Petri net for %s", processIdentifier), processIdentifier, filePath)
        }.collect()
    }
}
