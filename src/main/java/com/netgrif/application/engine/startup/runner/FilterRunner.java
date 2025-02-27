package com.netgrif.application.engine.startup.runner;

import com.netgrif.core.petrinet.domain.PetriNet;
import com.netgrif.core.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RunnerOrder(100)
@RequiredArgsConstructor
public class FilterRunner implements ApplicationEngineStartupRunner {

    public static final String FILTER_PETRI_NET_IDENTIFIER = "filter";
    public static final String PREFERRED_ITEM_NET_IDENTIFIER = "preference_item";

    private static final String FILTER_FILE_NAME = "engine-processes/filter.xml";
    private static final String PREFERRED_ITEM_FILE_NAME = "engine-processes/preference_item.xml";
    private static final String EXPORT_FILTER_FILE_NAME = "engine-processes/export_filters.xml";
    private static final String EXPORT_NET_IDENTIFIER = "export_filters";
    private static final String IMPORT_FILTER_FILE_NAME = "engine-processes/import_filters.xml";
    private static final String IMPORT_NET_IDENTIFIER = "import_filters";

    private final IPetriNetService petriNetService;
    private final ImportHelper helper;
    private final SystemUserRunner systemCreator;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createFilterNet();
        createPreferenceItemNet();
        createImportFiltersNet();
        createExportFiltersNet();
    }

    public Optional<PetriNet> createFilterNet() {
        return importProcess("Petri net for filters", FILTER_PETRI_NET_IDENTIFIER, FILTER_FILE_NAME);
    }

    public Optional<PetriNet> createPreferenceItemNet() {
        return importProcess("Petri net for filter preferences", PREFERRED_ITEM_NET_IDENTIFIER, PREFERRED_ITEM_FILE_NAME);
    }

    public Optional<PetriNet> createImportFiltersNet() {
        return importProcess("Petri net for importing filters", IMPORT_NET_IDENTIFIER, IMPORT_FILTER_FILE_NAME);
    }

    public Optional<PetriNet> createExportFiltersNet() {
        return importProcess("Petri net for exporting filters", EXPORT_NET_IDENTIFIER, EXPORT_FILTER_FILE_NAME);
    }

    protected Optional<PetriNet> importProcess(final String message, String netIdentifier, String netFileName) {
        PetriNet filter = petriNetService.getNewestVersionByIdentifier(netIdentifier);
        if (filter != null) {
            log.info("{} has already been imported.", message);
            return Optional.of(filter);
        }
        Optional<PetriNet> filterNet = helper.createNet(netFileName, VersionType.MAJOR, systemCreator.getLoggedSystem());
        if (filterNet.isEmpty()) {
            log.error("Import of {} failed!", message);
        }
        return filterNet;
    }

}
