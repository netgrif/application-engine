package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemView;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RunnerOrder(100)
@RequiredArgsConstructor
public class FilterRunner implements ApplicationEngineStartupRunner {

    @Autowired
    private ImportHelper helper;

    private static final String FILTER_FILE_NAME = "engine-processes/filter.xml";
    public static final String FILTER_PETRI_NET_IDENTIFIER = "filter";

    private static final String MENU_ITEM_FILE_NAME = "engine-processes/menu/menu_item.xml";
    public static final String MENU_NET_IDENTIFIER = "menu_item";

    private static final String EXPORT_FILTER_FILE_NAME = "engine-processes/export_filters.xml";
    private static final String EXPORT_NET_IDENTIFIER = "export_filters";

    private static final String IMPORT_FILTER_FILE_NAME = "engine-processes/import_filters.xml";
    private static final String IMPORT_NET_IDENTIFIER = "import_filters";


    @Override
    public void run(ApplicationArguments args) throws Exception {
        helper.importProcess("Petri net for filters", FILTER_PETRI_NET_IDENTIFIER, FILTER_FILE_NAME);
        createConfigurationNets();
        helper.importProcess("Petri net for filter preferences", MENU_NET_IDENTIFIER, MENU_ITEM_FILE_NAME);
        createImportFiltersNet();
        createExportFiltersNet();
    }

    Optional<PetriNet> createImportFiltersNet() {
        return helper.importProcess("Petri net for importing filters", IMPORT_NET_IDENTIFIER, IMPORT_FILTER_FILE_NAME);
    }

    Optional<PetriNet> createExportFiltersNet() {
        return helper.importProcess("Petri net for exporting filters", EXPORT_NET_IDENTIFIER, EXPORT_FILTER_FILE_NAME);
    }

    private List<PetriNet> createConfigurationNets() {
        return Arrays.stream(MenuItemView.values())
                .map(view -> {
                    String processIdentifier = view.getIdentifier() + "_configuration";
                    String filePath = String.format("engine-processes/menu/%s.xml", processIdentifier);
                    return helper.importProcess(
                            String.format("Petri net for %s", processIdentifier),
                            processIdentifier,
                            filePath
                    );
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
