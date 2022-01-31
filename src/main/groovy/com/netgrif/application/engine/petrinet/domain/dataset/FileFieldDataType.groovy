package com.netgrif.application.engine.petrinet.domain.dataset

enum FileFieldDataType {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    PDF("pdf");

    public final String extension;

    private FileFieldDataType(String extension) {
        this.extension = extension;
    }

    static FileFieldDataType resolveType(String extension) {
        for (FileFieldDataType item : values()) {
            if (item.extension == extension) {
                return item
            }
        }
    }
}
