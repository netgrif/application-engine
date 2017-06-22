package com.netgrif.workflow.importer.model;

import lombok.Data;

@Data
public class ImportTransition {

    private Long id;

    private String label;

    private RoleRef[] roleRef;

    private DataRef[] dataRef;

    private Integer y;

    private Integer x;

    private ImportTrigger[] trigger;

    private TransactionRef transactionRef;
}