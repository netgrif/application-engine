package com.netgrif.workflow.importer.model;

import lombok.Data;

@Data
public class ImportArc {

    private Long id;

    private Long destinationId;

    private Long sourceId;

    private Integer multiplicity;

    private String type;

    private BreakPoint breakPoint;
}