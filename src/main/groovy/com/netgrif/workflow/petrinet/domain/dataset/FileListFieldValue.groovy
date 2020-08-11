package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.configuration.ApplicationContextProvider
import com.netgrif.workflow.workflow.domain.FileStorageConfiguration
import org.apache.commons.lang3.tuple.ImmutablePair

class FileListFieldValue {

    private ArrayList<String> names

    private ArrayList<String> paths

    FileListFieldValue() {
        this.names = new ArrayList<>()
        this.paths = new ArrayList<>()
    }

    ArrayList<String> getNames() {
        return names
    }

    ArrayList<String> getPaths() {
        return paths
    }

    String getPath(String caseId, String fieldId, String name) {
        FileStorageConfiguration fileStorageConfiguration = ApplicationContextProvider.getBean("fileStorageConfiguration")
        return "${fileStorageConfiguration.getStoragePath()}/${caseId}/${fieldId}/${name}"
    }

    @Override
    String toString() {
        return paths.toString()
    }
}
