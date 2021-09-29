package com.netgrif.workflow.startup


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FinisherRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FinisherRunner)

    @Override
    void run(String... strings) throws Exception {
        log.info("+----------------------------+")
        log.info("| Netgrif Application Engine |")
        log.info("+----------------------------+")
    }
}