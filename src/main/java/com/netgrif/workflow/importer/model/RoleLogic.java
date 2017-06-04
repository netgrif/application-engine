package com.netgrif.workflow.importer.model;

public interface RoleLogic {
    public Boolean getPerform();

    public void setPerform(Boolean assignToSelf);

    public Boolean getDelegate();

    public void setDelegate(Boolean assignToOther);
}