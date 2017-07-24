package com.netgrif.workflow.importer.model;

import com.netgrif.workflow.importer.InvalidXmlException;
import lombok.Data;

import java.util.Arrays;

@Data
public class ImportTransition {

    private Long id;

    private String label;

    private RoleRef[] roleRef;

    private ImportDataGroup[] dataGroup;

    private Integer y;

    private Integer x;

    private ImportTrigger[] trigger;

    private TransactionRef transactionRef;

    private Integer priority;

    public Integer getPriority() {
        return priority == null ? 0 : priority;
    }

    public DataRef[] getDataRef() {
        try {
            if (dataGroup == null)
                return null;
            return Arrays.stream(dataGroup)
                    .map(ImportDataGroup::getDataRef)
                    .flatMap(Arrays::stream)
                    .toArray(DataRef[]::new);
        } catch (NullPointerException e) {
            throw new InvalidXmlException("DataGroup of Transition[id:" + id + "] element has no dataRefs", e);
        }
    }
}