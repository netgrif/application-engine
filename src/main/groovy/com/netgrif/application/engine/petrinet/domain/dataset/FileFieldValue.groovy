package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.configuration.ApplicationContextProvider
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration

class FileFieldValue implements Serializable {

    private static final long serialVersionUID = 1299918326436821185L

    private String name

    private String path

    private String previewPath

    FileFieldValue() {
    }

    FileFieldValue(String name, String path) {
        this.name = name
        this.path = path
    }

    FileFieldValue(String name, String path, String previewPath) {
        this.name = name
        this.path = path
        this.previewPath = previewPath
    }

    static FileFieldValue fromString(String value) {
        if (!value.contains(":"))
            return new FileFieldValue(value, null)

        String[] parts = value.split(":", 2)
        return new FileFieldValue(parts[0], parts[1])
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getPath() {
        return path
    }

    void setPath(String path) {
        this.path = path
    }

    String getPath(String caseId, String fieldId) {
        FileStorageConfiguration fileStorageConfiguration = ApplicationContextProvider.getBean("fileStorageConfiguration") as FileStorageConfiguration
        return "${fileStorageConfiguration.getStoragePath()}/${caseId}/${fieldId}-${name}"
    }

    String getPreviewPath(String caseId, String fieldId, boolean isRemote) {
        if (isRemote) {
            return "${caseId}-${fieldId}-${name}.file_preview"
        }
        FileStorageConfiguration fileStorageConfiguration = ApplicationContextProvider.getBean("fileStorageConfiguration") as FileStorageConfiguration
        return "${fileStorageConfiguration.getStoragePath()}/file_preview/${caseId}/${fieldId}-${name}"
    }

    String getPreviewPath() {
        return previewPath
    }

    void setPreviewPath(String previewPath) {
        this.previewPath = previewPath
    }

    @Override
    String toString() {
        return path
    }
}
