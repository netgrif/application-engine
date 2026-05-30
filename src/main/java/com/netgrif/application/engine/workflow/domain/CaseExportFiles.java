package com.netgrif.application.engine.workflow.domain;

import java.util.*;

public class CaseExportFiles {

    private final Map<String, List<StorageFieldWithFileNames>> caseFileMapping = new HashMap<>();

    public void addFieldFilenames(String caseId, StorageFieldWithFileNames storageField) {
        List<StorageFieldWithFileNames> emptyFieldMapping = new ArrayList<>();
        List<StorageFieldWithFileNames> fieldMapping = caseFileMapping.putIfAbsent(caseId, emptyFieldMapping);
        if (fieldMapping == null) {
            fieldMapping = emptyFieldMapping;
        }
        fieldMapping.add(storageField);
    }

    public Set<String> getCaseIds() {
        return caseFileMapping.keySet();
    }

    public List<StorageFieldWithFileNames> getFieldsOfCase(String caseId) {
        return caseFileMapping.get(caseId);
    }
}
