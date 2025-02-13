package com.netgrif.application.engine.startup

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class FilterRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ImportHelper helper

    private static final String FILTER_FILE_NAME = "engine-processes/filter.xml"
    public static final String FILTER_PETRI_NET_IDENTIFIER = "filter"

    private static final String PREFERRED_ITEM_FILE_NAME = "engine-processes/preference_item.xml"
    public static final String PREFERRED_ITEM_NET_IDENTIFIER = "preference_item"

    private static final String EXPORT_FILTER_FILE_NAME = "engine-processes/export_filters.xml"
    private static final String EXPORT_NET_IDENTIFIER = "export_filters"

    private static final String IMPORT_FILTER_FILE_NAME = "engine-processes/import_filters.xml"
    private static final String IMPORT_NET_IDENTIFIER = "import_filters"

    @Override
    void run(String... args) throws Exception {
        helper.importProcess("Petri net for filters", FILTER_PETRI_NET_IDENTIFIER, FILTER_FILE_NAME)
        helper.importProcess("Petri net for filter preferences", PREFERRED_ITEM_NET_IDENTIFIER, PREFERRED_ITEM_FILE_NAME)
        helper.importProcess("Petri net for importing filters", IMPORT_NET_IDENTIFIER, IMPORT_FILTER_FILE_NAME)
        helper.importProcess("Petri net for exporting filters", EXPORT_NET_IDENTIFIER, EXPORT_FILTER_FILE_NAME)
    }
}
