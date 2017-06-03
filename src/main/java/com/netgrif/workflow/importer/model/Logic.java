package com.netgrif.workflow.importer.model;

import com.netgrif.workflow.importer.model.datalogic.ImportAutoPlus;

public class Logic implements RoleLogic, DataLogic {
    private Boolean editable;
    private Boolean visible;
    private ImportAutoPlus autoPlus;
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

    public ImportAutoPlus getAutoPlus() {
        return autoPlus;
    }

    public void setAutoPlus(ImportAutoPlus autoPlus) {
        this.autoPlus = autoPlus;
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