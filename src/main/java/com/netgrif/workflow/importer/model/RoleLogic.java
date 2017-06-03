package com.netgrif.workflow.importer.model;

public interface RoleLogic {
    public Boolean getAssign();

    public void setAssign(Boolean assignToSelf);

    public Boolean getDelegate();

    public void setDelegate(Boolean assignToOther);
}