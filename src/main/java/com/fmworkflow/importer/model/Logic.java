package com.fmworkflow.importer.model;

import com.fmworkflow.importer.model.datalogic.ImportPlusYears;

public class Logic implements RoleLogic, DataLogic {
    private Boolean editable;
    private Boolean visible;
    private ImportPlusYears plusYears;
    private Boolean required;

    private Boolean assign;
    private Boolean delegate;

//  DATA
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

    public ImportPlusYears getPlusYears() {
        return plusYears;
    }

    public void setPlusYears(ImportPlusYears plusYears) {
        this.plusYears = plusYears;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    //  ROLE
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