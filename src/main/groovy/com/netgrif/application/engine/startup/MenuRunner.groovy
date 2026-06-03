package com.netgrif.application.engine.startup

import com.netgrif.application.engine.menu.domain.MenuItemConstants
import com.netgrif.application.engine.menu.domain.MenuItemViewType
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class MenuRunner extends AbstractOrderedCommandLineRunner {

    private static final String MENU_ITEM_FILE_NAME = "engine-processes/menu/menu_item.xml"

    @Autowired
    private ImportHelper helper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SystemUserRunner systemCreator

    @Override
    void run(String... args) throws Exception {
        createConfigurationNets()
        helper.importProcess("Petri net for menu item", MenuItemConstants.PROCESS_IDENTIFIER, MENU_ITEM_FILE_NAME)
    }

    private void createConfigurationNets() {
        MenuItemViewType.values().each { view ->
            String processIdentifier = view.getIdentifier() + "_configuration"
            String filePath = String.format("engine-processes/menu/%s.xml", processIdentifier)
            helper.importProcess(String.format("Petri net for %s", processIdentifier), processIdentifier, filePath)
        }.collect()
    }
}
