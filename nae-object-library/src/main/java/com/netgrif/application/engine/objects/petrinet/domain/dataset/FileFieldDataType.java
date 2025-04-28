package com.netgrif.application.engine.objects.petrinet.domain.dataset;

public enum FileFieldDataType {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    PDF("pdf");

    public final String extension;

    FileFieldDataType(String extension) {
        this.extension = extension;
    }

    public static FileFieldDataType resolveType(String extension) {
        for (FileFieldDataType item : values()) {
            if (item.extension.equals(extension)) {
                return item;
            }
        }
        return null;
    }

    public static FileFieldDataType resolveTypeFromName(String name) {
        int dot = name.lastIndexOf(".");
        return resolveType((dot == -1) ? "" : name.substring(dot + 1));
    }
}
