package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.files.minio.StorageConfigurationProperties;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
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

    private final StorageConfigurationProperties storageConfigurationProperties;

    private final StorageConfigurationProperties fileStorageConfiguration;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Creating storage folder");
        File storage = new File(fileStorageConfiguration.getPath() + File.separator + "uploadedModels" + File.separator + "model.txt");
        storage.getParentFile().mkdirs();

        if (storageConfigurationProperties.isClean()) {
            log.info("Removing files from storage folder and it's sub-folders");
            purgeDirectory(new File(fileStorageConfiguration.getPath()));
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
