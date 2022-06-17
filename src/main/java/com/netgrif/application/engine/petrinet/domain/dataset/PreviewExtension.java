package com.netgrif.application.engine.petrinet.domain.dataset;

public enum PreviewExtension {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    PDF("pdf");

    public final String extension;

    PreviewExtension(String extension) {
        this.extension = extension;
    }

    public static PreviewExtension resolveType(String extension) {
        for (PreviewExtension item : values()) {
            if (item.extension.equals(extension)) {
                return item;
            }
        }
        return null;
    }
}
