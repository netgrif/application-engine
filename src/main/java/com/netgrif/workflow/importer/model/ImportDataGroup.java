package com.netgrif.workflow.importer.model;

import lombok.Data;

@Data
public class ImportDataGroup {

    private Long id;

    private String title;

    private String alignment;

    private Boolean stretch;

    private DataRef[] dataRef;
}