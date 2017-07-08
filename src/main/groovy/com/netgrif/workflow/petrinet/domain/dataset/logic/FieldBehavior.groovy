package com.netgrif.workflow.petrinet.domain.dataset.logic

enum FieldBehavior {
    REQUIRED("required"),
    OPTIONAL("optional"),
    VISIBLE("visible"),
    EDITABLE("editable"),
    HIDDEN("hidden"),
    ANTONYM_SETUP("",true);


    private final String name
    private FieldBehavior[] antonyms

    FieldBehavior(String name) {
        this.name = name
    }

    FieldBehavior(String name, boolean populate) {
        this.name = name
        if (populate) {
            REQUIRED.setAntonyms()
            OPTIONAL.setAntonyms()
            VISIBLE.setAntonyms()
            EDITABLE.setAntonyms()
            HIDDEN.setAntonyms()
        }
    }

    private FieldBehavior[] addAntonyms() {
        switch (name) {
            case "required":
                return (FieldBehavior[]) [VISIBLE, HIDDEN, OPTIONAL].toArray()
            case "optional":
                return (FieldBehavior[]) [REQUIRED].toArray()
            case "visible":
                return (FieldBehavior[]) [REQUIRED, EDITABLE, HIDDEN].toArray()
            case "editable":
                return (FieldBehavior[]) [VISIBLE, HIDDEN].toArray()
            case "hidden":
                return (FieldBehavior[]) [EDITABLE, VISIBLE, REQUIRED].toArray()
            default:
                return null
        }
    }

    public static FieldBehavior fromString(String string) {
        return FieldBehavior.valueOf(string.toUpperCase());
    }

    FieldBehavior[] getAntonyms() {
        return antonyms
    }

    private void setAntonyms() {
        this.antonyms = addAntonyms()
    }

    public String toString() {
        return this.name
    }
}