package com.netgrif.application.engine.workflow.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileStorageConfiguration {

    @Value("${nae.storage.path:storage}")
    private static String storagePath;

    @Value("${nae.storage.archived:storage/uploadedModels/}")
    private static String storageArchived;

    public String getStoragePath() {
        return storagePath;
    }

    public String getStorageArchived() {
        return storageArchived;
    }

    public static String getPath(String caseId, String fieldId, String name) {
        return String.format("%s/%s-%s-%s", storagePath, caseId, fieldId, name);
    }

    public static String getPreviewPath(String caseId, String fieldId, String name) {
        return String.format("%s/file_preview/%s-%s-%s", storagePath, caseId, fieldId, name);
    }
}
