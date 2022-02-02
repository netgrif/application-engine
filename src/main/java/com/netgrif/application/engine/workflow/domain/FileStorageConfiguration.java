package com.netgrif.application.engine.workflow.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileStorageConfiguration {

    @Value("${nae.storage.path:storage}")
    private String storagePath;

    @Value("${nae.storage.archived:storage/uploadedModels/}")
    private String storageArchived;

    public String getStoragePath() {
        return storagePath;
    }

    public String getStorageArchived() {
        return storageArchived;
    }
}
