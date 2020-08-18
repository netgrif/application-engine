package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.configuration.ApplicationContextProvider
import com.netgrif.workflow.workflow.domain.FileStorageConfiguration

class FileListFieldValue {

    private HashSet<FileFieldValue> namesPaths

    FileListFieldValue() {
        this.namesPaths = new HashSet<>()
    }

    HashSet<FileFieldValue> getNamesPaths() {
        return this.namesPaths
    }

    static FileListFieldValue fromString(String value) {
        FileListFieldValue newVal = new FileListFieldValue()
        String[] parts = value.split(",")
        for (String part : parts) {
            if (!part.contains(":"))
                newVal.getNamesPaths().add(new FileFieldValue(part, null))
            else {
                String[] filePart = part.split(":", 2)
                newVal.getNamesPaths().add(new FileFieldValue(filePart[0], filePart[1]))
            }
        }
        return newVal
    }

    static String getPath(String caseId, String fieldId, String name) {
        FileStorageConfiguration fileStorageConfiguration = ApplicationContextProvider.getBean("fileStorageConfiguration")
        return "${fileStorageConfiguration.getStoragePath()}/${caseId}/${fieldId}/${name}"
    }

    @Override
    String toString() {
        return namesPaths.toString()
    }
}
