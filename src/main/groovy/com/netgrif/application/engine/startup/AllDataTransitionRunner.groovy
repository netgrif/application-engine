package com.netgrif.application.engine.startup

import com.netgrif.application.engine.importer.model.Document
import com.netgrif.application.engine.importer.model.Transition
import com.netgrif.application.engine.importer.service.AllDataConfiguration
import com.netgrif.application.engine.importer.service.Importer
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

import javax.inject.Provider

@Slf4j
@Component
@CompileStatic
class AllDataTransitionRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private AllDataConfiguration configuration
    @Autowired
    private Provider<Importer> importerProvider
    @Value("classpath:petriNets/engine-processes/all_data_transition_configuration.xml")
    private Resource configurationFile

    protected Importer getImporter() {
        return importerProvider.get()
    }

    @Override
    void run(String... strings) throws Exception {
        log.info("Importing 'All Data' transition configuration")

        if (configuration.allData) {
            log.info("'All Data' transition configuration already exists")
            return
        }
        InputStream netStream = configurationFile.inputStream
        Document document = getImporter().unmarshallXml(netStream)

        Transition allData = document.getTransition().first()
        configuration.allData = allData

        log.info("'All Data' transition configuration created")
    }
}