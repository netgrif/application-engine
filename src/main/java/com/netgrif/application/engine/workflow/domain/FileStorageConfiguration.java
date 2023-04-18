package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.configuration.properties.NaeStorageProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
public class FileStorageConfiguration {

    private static FileStorageConfiguration instance;

    @Autowired
    private NaeStorageProperties properties;

    public FileStorageConfiguration() {
        instance = this;
    }

    public static FileStorageConfiguration getInstance() {
        return instance;
    }

    public static String getPath(String caseId, String fieldId, String name) {
        return String.format("%s/%s-%s-%s", instance.properties.getPath(), caseId, fieldId, name);
    }

    public static String getPreviewPath(String caseId, String fieldId, String name) {
        return String.format("%s/" + instance.properties.getFilePreviewFolder() + "/%s-%s-%s", instance.properties.getPath(), caseId, fieldId, name);
    }
}
