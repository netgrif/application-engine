package com.netgrif.application.engine.workflow.domain.dataset;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileFieldValue implements Serializable {

    private static final long serialVersionUID = 1299918326436821185L;

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
