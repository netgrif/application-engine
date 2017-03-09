package com.fmworkflow.importer.model;

public class Logic implements RoleLogic, DataLogic {
    private Boolean editable;
    private Boolean visible;

    private Boolean assignToSelf;
    private Boolean assignToOther;

    public Boolean getEditable() {
        return editable != null && editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Boolean getVisible() {
        return visible != null && visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getAssignToSelf() {
        return assignToSelf != null && assignToSelf;
    }

    public void setAssignToSelf(Boolean assignToSelf) {
        this.assignToSelf = assignToSelf;
    }

    public Boolean getAssignToOther() {
        return assignToOther != null && assignToOther;
    }

    public void setAssignToOther(Boolean assignToOther) {
        this.assignToOther = assignToOther;
    }
}