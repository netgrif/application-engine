package com.netgrif.workflow.importer.model;



public interface DataLogic {
    void setBehavior(String[] behavior);
    String[] getBehavior();

    void setAction(ImportAction[] action);
    ImportAction[] getAction();
}