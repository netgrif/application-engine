package com.netgrif.workflow.workflow.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileStorageConfiguration {

    @Value("${storage.path:storage}")
    private String storagePath;

    @Value("${storage.archived:storage/uploadedModels/}")
    private String storageArchived;

    public String getStoragePath() {
        return storagePath;
    }

    public String getStorageArchived() {
        return storageArchived;
    }
}
