package com.netgrif.workflow.importer.model;

public class Logic implements RoleLogic, DataLogic {

    private String[] behavior;

    private ImportAction[] action;


    private Boolean perform;

    private Boolean delegate;

//  DATA
    @Override
    public String[] getBehavior() {
        return behavior;
    }

    @Override
    public void setBehavior(String[] behavior) {
        this.behavior = behavior;
    }

    @Override
    public ImportAction[] getAction() {
        return action;
    }

    @Override
    public void setAction(ImportAction[] action) {
        this.action = action;
    }

    //  ROLE
    public Boolean getPerform() {
        return perform != null && perform;
    }

    public void setPerform(Boolean assignToSelf) {
        this.perform = assignToSelf;
    }

    public Boolean getDelegate() {
        return delegate != null && delegate;
    }

    public void setDelegate(Boolean assignToOther) {
        this.delegate = assignToOther;
    }
}