package com.netgrif.workflow.importer.model;

public class Logic implements RoleLogic, DataLogic {

    private String[] behavior;

    private String[] actions;


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
    public String[] getActions() {
        return actions;
    }

    @Override
    public void setActions(String[] actions) {
        this.actions = actions;
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