package com.netgrif.workflow.petrinet.domain.dataset

class FileFieldValue {

    private String name

    private String path

    FileFieldValue() {
    }

    FileFieldValue(String name, String path) {
        this.name = name
        this.path = path
    }

    public static FileFieldValue fromString(String value) {
        if (!value.contains(":"))
            return new FileFieldValue(value, null)

        String[] parts = value.split(":", 2)
        return new FileFieldValue(parts[0], parts[1])
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getPath() {
        return path
    }

    String getPath(String caseId, String fieldId) {
        return "storage/${caseId}-${fieldId}-${name}"
    }

    void setPath(String path) {
        this.path = path
    }


    @Override
    public String toString() {
        return path
    }
}
