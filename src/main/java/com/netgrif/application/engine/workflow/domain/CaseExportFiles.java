package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.StorageField;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

public class CaseExportFiles {

    private final Map<String, List<ImmutablePair<StorageField<?>, Set<String>>>> caseFileMapping = new HashMap<>();

    public void addFieldFilenames(String caseId, StorageField<?> storageField, Set<String> filenames) {
        if (!caseFileMapping.containsKey(caseId)) {
            caseFileMapping.put(caseId, new ArrayList<>());
        }
        List<ImmutablePair<StorageField<?>, Set<String>>> fieldMapping = caseFileMapping.get(caseId);
        if (fieldMapping == null) {
            fieldMapping = new ArrayList<>();
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
