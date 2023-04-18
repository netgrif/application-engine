package com.netgrif.application.engine.startup

import com.netgrif.application.engine.configuration.properties.NaeStorageProperties
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Slf4j
@Component
@Profile("!test")
@CompileStatic
class StorageRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private NaeStorageProperties naeStorageProperties

    @Autowired
    private FileStorageConfiguration fileStorageConfiguration

    @Override
    void run(String... strings) throws Exception {
        log.info("Creating storage folder")
        File storage = new File("${fileStorageConfiguration.getProperties().getPath()}/uploadedModels/model.txt")
        storage.getParentFile().mkdirs()

        if (naeStorageProperties.clean) {
            log.info("Removing files from storage folder and it's sub-folders")
            purgeDirectory(new File(fileStorageConfiguration.getProperties().getPath()))
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