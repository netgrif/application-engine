package com.netgrif.workflow.importer.model;



public interface DataLogic {
    void setBehavior(String[] behavior);
    String[] getBehavior();

    void setAction(String[] action);
    String[] getAction();
}