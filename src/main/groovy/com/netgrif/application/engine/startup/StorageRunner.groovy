package com.netgrif.application.engine.startup

import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class StorageRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StorageRunner)

    @Value('${nae.storage.clean}')
    private boolean cleanStorage

    @Autowired
    private FileStorageConfiguration fileStorageConfiguration

    @Override
    void run(String... strings) throws Exception {
        log.info("Creating storage folder")
        File storage = new File("${fileStorageConfiguration.getStoragePath()}/uploadedModels/model.txt")
        storage.getParentFile().mkdirs()

        if (cleanStorage) {
            log.info("Removing files from storage folder and it's sub-folders")
            purgeDirectory(new File(fileStorageConfiguration.getStoragePath()))
        }

        log.info("Creating log folder")
        File logDir = new File("log/log.txt")
        logDir.getParentFile().mkdirs()
    }

    private static void purgeDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                purgeDirectory(file)
            else
                file.delete()
        }
    }
}