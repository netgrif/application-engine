package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.StorageField;
import lombok.Data;

import java.util.Set;

@Data
public class StorageFieldWithFileNames {

    private StorageField<?> field;
    private Set<String> fileNames;

    public StorageFieldWithFileNames(StorageField<?> field, Set<String> fileNames) {
        this.field = field;
        this.fileNames = fileNames;
    }
}
