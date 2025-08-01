package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(100)
@RequiredArgsConstructor
public class FilterRunner implements ApplicationEngineStartupRunner {

    private final ImportHelper helper;

    private static final String FILTER_FILE_NAME = "engine-processes/filter.xml";
    public static final String FILTER_PETRI_NET_IDENTIFIER = "filter";

    private static final String EXPORT_FILTER_FILE_NAME = "engine-processes/export_filters.xml";
    public static final String EXPORT_NET_IDENTIFIER = "export_filters";

    private static final String IMPORT_FILTER_FILE_NAME = "engine-processes/import_filters.xml";
    public static final String IMPORT_NET_IDENTIFIER = "import_filters";


    @Override
    public void run(ApplicationArguments args) throws Exception {
        helper.importProcessOnce("Petri net for filters", FILTER_PETRI_NET_IDENTIFIER, FILTER_FILE_NAME);
        helper.importProcessOnce("Petri net for importing filters", IMPORT_NET_IDENTIFIER, IMPORT_FILTER_FILE_NAME);
        helper.importProcessOnce("Petri net for exporting filters", EXPORT_NET_IDENTIFIER, EXPORT_FILTER_FILE_NAME);
    }

}
