package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@Profile("!test")
@RunnerOrder(20)
@RequiredArgsConstructor
class StorageRunner implements ApplicationEngineStartupRunner {

    @Value("${nae.storage.clean}")
    private boolean cleanStorage;

    private final FileStorageConfiguration fileStorageConfiguration;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Creating storage folder");
        File storage = new File(fileStorageConfiguration.getStoragePath() + File.separator + "uploadedModels" + File.separator + "model.txt");
        storage.getParentFile().mkdirs();

        if (cleanStorage) {
            log.info("Removing files from storage folder and it's sub-folders");
            purgeDirectory(new File(fileStorageConfiguration.getStoragePath()));
        }

        log.info("Creating log folder");
        File logDir = new File("log" + File.separator + "log.txt");
        logDir.getParentFile().mkdirs();
    }

    private static void purgeDirectory(File dir) {
        if (dir == null || !dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory())
                purgeDirectory(file);
            else
                file.delete();
        }
    }
}
