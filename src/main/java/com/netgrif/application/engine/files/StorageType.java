package com.netgrif.application.engine.files;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum StorageType {

    MINIO("MINIO"),
    LOCAL("LOCAL");

    private final String type;

    StorageType(String type) {
        this.type = type;
    }

    public static StorageType fromString(String value) {
        Optional<StorageType> storageType = Arrays.stream(StorageType.values()).filter(it -> it.getType().equals(value)).findFirst();
        if (storageType.isPresent()) {
            return storageType.get();
        }
        throw new NullPointerException("Storage type with value: "+ value + " not found.");
    }
}
