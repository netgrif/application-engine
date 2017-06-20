package com.netgrif.workflow.petrinet.domain.dataset.logic

enum DataBehavior {
    REQUIRED("required"),
    OPTIONAL("optional"),
    VISIBLE("visible"),
    EDITABLE("editable")


    private String name

    DataBehavior(String name) {
        this.name = name
    }

    public static DataBehavior fromString(String string){
        return DataBehavior.valueOf(string.toUpperCase());
    }

    public String toString(){
        return this.name
    }
}