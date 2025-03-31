package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.StorageField;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

public class CaseExportFiles {

    private final Map<String, List<ImmutablePair<StorageField<?>, Set<String>>>> caseFileMapping = new HashMap<>();

    public void addFieldFilenames(String caseId, StorageField<?> storageField, Set<String> filenames) {
        List<ImmutablePair<StorageField<?>, Set<String>>> emptyFieldMapping = new ArrayList<>();
        List<ImmutablePair<StorageField<?>, Set<String>>> fieldMapping = caseFileMapping.putIfAbsent(caseId, emptyFieldMapping);
        if (fieldMapping == null) {
            fieldMapping = emptyFieldMapping;
        }
        fieldMapping.add(new ImmutablePair<>(storageField, filenames));
    }

    public Set<String> getCaseIds() {
        return caseFileMapping.keySet();
    }

    public List<ImmutablePair<StorageField<?>, Set<String>>> getFieldsOfCase(String caseId) {
        return caseFileMapping.get(caseId);
    }
}
