package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Data;

@Data
public class FileFieldValue {

    private String name;
    private String path;

    public FileFieldValue() {
    }

    public FileFieldValue(String name, String path) {
        this();
        this.name = name;
        this.path = path;
    }

    public static FileFieldValue fromString(String value) {
        if (!value.contains(":")) {
            return new FileFieldValue(value, null);
        }
        String[] parts = value.split(":", 2);
        return new FileFieldValue(parts[0], parts[1]);
    }

    @Override
    public String toString() {
        return path;
    }
}
