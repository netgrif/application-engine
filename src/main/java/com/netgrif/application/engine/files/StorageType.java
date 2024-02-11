package com.netgrif.application.engine.files;

import lombok.Getter;

public enum StorageType {

    MINIO("MINIO");

    @Getter
    String type;

    StorageType(String type) {
        this.type = type;
    }
}
