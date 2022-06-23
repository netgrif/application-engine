package com.netgrif.application.engine.workflow.domain;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class FileStorageConfiguration {

    private static FileStorageConfiguration instance;

    public FileStorageConfiguration() {
        instance = this;
    }

    @Value("${nae.storage.path:storage}")
    private String storagePath;

    @Value("${nae.storage.archived:storage/uploadedModels/}")
    private String storageArchived;

    public static FileStorageConfiguration getInstance() {
        return instance;
    }

    public static String getPath(String caseId, String fieldId, String name) {
        return String.format("%s/%s-%s-%s", instance.storagePath, caseId, fieldId, name);
    }

    public static String getPreviewPath(String caseId, String fieldId, String name) {
        return String.format("%s/file_preview/%s-%s-%s", instance.storagePath, caseId, fieldId, name);
    }
}
