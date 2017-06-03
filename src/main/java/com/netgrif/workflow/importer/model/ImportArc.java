package com.netgrif.workflow.importer.model;

public class ImportArc {
    private Long id;
    private Long destinationId;
    private Long sourceId;
    private Integer multiplicity;
    private String type;
    private BreakPoint breakPoint;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(Long destinationId) {
        this.destinationId = destinationId;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Integer getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(Integer multiplicity) {
        this.multiplicity = multiplicity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BreakPoint getBreakPoint() {
        return breakPoint;
    }

    public void setBreakPoint(BreakPoint breakPoint) {
        this.breakPoint = breakPoint;
    }
}
