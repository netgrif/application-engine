package com.netgrif.workflow.importer.model;



public interface DataLogic {
    void setBehavior(String[] behavior);
    String[] getBehavior();

    void setActions(String[] actions);
    String[] getActions();
}