package com.netgrif.application.engine.startup

import com.netgrif.application.engine.petrinet.service.PetriNetExistsException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Slf4j
@Component
class ProcessRunner extends AbstractOrderedCommandLineRunner {

    private final ImportHelper helper
    private final IPetriNetService petriNetService
    private SystemUserRunner systemUserRunner

    static final String PROCESS_XML_FILENAME = 'process.xml'

    ProcessRunner(ImportHelper helper, IPetriNetService petriNetService) {
        this.helper = helper
        this.petriNetService = petriNetService
    }

    @Override
    void run(String... args) throws Exception {
        log.info("Import of system process Process started")
        try(InputStream netStream = new ClassPathResource("petriNets/engine-processes/$PROCESS_XML_FILENAME" as String).inputStream) {
            def outcome = petriNetService.importPetriNet(netStream, systemUserRunner.loggedSystem)
            assert outcome.net
            log.info("Process Process imported")
        } catch (PetriNetExistsException ignored) {
            log.info("Process Process already exists")
        }
        log.info("Import of system process Process ended")
    }

    @Autowired
    void setSystemUserRunner(SystemUserRunner systemUserRunner) {
        this.systemUserRunner = systemUserRunner
    }
}
