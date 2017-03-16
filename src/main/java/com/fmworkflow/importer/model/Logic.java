package com.fmworkflow.importer.model;

public class Logic implements RoleLogic, DataLogic {
    private Boolean editable;
    private Boolean visible;

    private Boolean assign;
    private Boolean delegate;

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

    public Boolean getAssign() {
        return assign != null && assign;
    }

    public void setAssign(Boolean assignToSelf) {
        this.assign = assignToSelf;
    }

    public Boolean getDelegate() {
        return delegate != null && delegate;
    }

    public void setDelegate(Boolean assignToOther) {
        this.delegate = assignToOther;
    }
}