package com.netgrif.application.engine.startup

import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FinisherRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FinisherRunner)

    @Autowired
    IUriService uriService

    @Autowired
    IPetriNetService petriNetService

    @Override
    void run(String... strings) throws Exception {
        log.info("+----------------------------+")
        log.info("| Netgrif Application Engine |")
        log.info("+----------------------------+")
    }
}
