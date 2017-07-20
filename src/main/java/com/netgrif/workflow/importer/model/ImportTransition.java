package com.netgrif.workflow.importer.model;

import lombok.Data;

import java.util.Arrays;

@Data
public class ImportTransition {

    private Long id;

    private String label;

    private RoleRef[] roleRef;

    private ImportDataGroup[] dataGroups;

    private Integer y;

    private Integer x;

    private ImportTrigger[] trigger;

    private TransactionRef transactionRef;

    private Integer priority;

    public Integer getPriority() {
        return priority == null ? 0 : priority;
    }

    public DataRef[] getDataRef() {
        if (dataGroups == null)
            return null;
        return Arrays.stream(dataGroups)
                .map(ImportDataGroup::getDataRefs)
                .toArray(DataRef[]::new);
    }
}