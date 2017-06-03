package com.netgrif.workflow.importer.model;

public class ImportTransition {
    private Long id;
    private String label;
    private RoleRef[] roleRef;
    private DataRef[] dataRef;
    private Integer y;
    private Integer x;
    private ImportTrigger[] trigger;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public RoleRef[] getRoleRef() {
        return roleRef;
    }

    public void setRoleRef(RoleRef[] roleRef) {
        this.roleRef = roleRef;
    }

    public DataRef[] getDataRef() {
        return dataRef;
    }

    public void setDataRef(DataRef[] dataRef) {
        this.dataRef = dataRef;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public ImportTrigger[] getTrigger() {
        return trigger;
    }

    public void setTrigger(ImportTrigger[] trigger) {
        this.trigger = trigger;
    }
}