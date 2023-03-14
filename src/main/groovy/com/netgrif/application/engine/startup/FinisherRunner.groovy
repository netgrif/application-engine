package com.netgrif.application.engine.startup

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
class FinisherRunner extends AbstractOrderedCommandLineRunner {

    @Override
    void run(String... strings) throws Exception {
        log.info("+----------------------------+")
        log.info("| Netgrif Application Engine |")
        log.info("+----------------------------+")
    }
}
