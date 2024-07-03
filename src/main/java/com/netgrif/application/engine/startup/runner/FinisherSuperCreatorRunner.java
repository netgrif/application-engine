package com.netgrif.application.engine.startup


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
class FinisherRunnerSuperCreator extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FinisherRunnerSuperCreator)

    @Autowired
    private SuperCreator superCreator

    @Override
    void run(String... strings) throws Exception {
       superCreator.setAllToSuperUser()
        log.info("Super Creator update")
    }

}