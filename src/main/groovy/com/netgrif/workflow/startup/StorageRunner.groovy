package com.netgrif.workflow.startup

import org.apache.log4j.Logger
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class StorageRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = Logger.getLogger(StorageRunner)

    @Override
    void run(String... strings) throws Exception {
        log.info("Creating storage folder")
        File storage = new File("storage/generated/start.txt")
        storage.getParentFile().mkdirs()
    }
}