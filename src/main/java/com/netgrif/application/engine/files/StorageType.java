package com.netgrif.application.engine.files;

import lombok.Getter;

public enum StorageType {

    MINIO("MINIO"),
    LOCAL("LOCAL");

    @Getter
    String type;

    StorageType(String type) {
        this.type = type;
    }
}
