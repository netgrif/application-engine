package com.netgrif.application.engine.petrinet.domain.dataset;

class MinIoStorage extends Storage {
    private String bucket

    MinIoStorage() {
        super(StorageType.MINIO)
    }

    String getBucket() {
        return bucket
    }

    void setBucket(String bucket) {
        this.bucket = bucket
    }
}
