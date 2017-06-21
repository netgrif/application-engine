package com.netgrif.workflow.petrinet.domain.dataset.logic

enum FieldBehavior {
    REQUIRED("required"),
    OPTIONAL("optional"),
    VISIBLE("visible"),
    EDITABLE("editable")


    private String name

    FieldBehavior(String name) {
        this.name = name
    }

    public static FieldBehavior fromString(String string){
        return FieldBehavior.valueOf(string.toUpperCase());
    }

    public String toString(){
        return this.name
    }
}