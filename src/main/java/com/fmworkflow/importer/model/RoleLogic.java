package com.fmworkflow.importer.model;

public interface RoleLogic {
    public Boolean getAssignToSelf();

    public void setAssignToSelf(Boolean assignToSelf);

    public Boolean getAssignToOther();

    public void setAssignToOther(Boolean assignToOther);
}