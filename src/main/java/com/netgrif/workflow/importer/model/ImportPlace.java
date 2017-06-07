package com.netgrif.workflow.importer.model;

import lombok.Data;

@Data
public class ImportPlace {

    private Long id;

    private Boolean isStatic;

    private Integer tokens;

    private String label;

    private Integer y;

    private Integer x;
}