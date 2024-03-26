package com.netgrif.application.engine.petrinet.domain.dataset

class FileFieldValue implements Serializable {

    private static final long serialVersionUID = 1299918326436821185L;

    private String name

    private String path

    private String previewPath

    FileFieldValue() {
    }

    FileFieldValue(String name, String path) {
        this.name = name
        this.path = path
    }

    FileFieldValue(String name, String path, String previewPath) {
        this.name = name
        this.path = path
        this.previewPath = previewPath
    }

    static FileFieldValue fromString(String value) {
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

    void setPath(String path) {
        this.path = path
    }


    @Override
    String toString() {
        return path
    }
}
