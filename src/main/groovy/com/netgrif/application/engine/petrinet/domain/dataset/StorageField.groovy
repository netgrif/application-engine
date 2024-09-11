package com.netgrif.application.engine.petrinet.domain.dataset

abstract class StorageField<T> extends Field<T> {
    private String storageType
    private Remote remote

    StorageField() {
        super()
    }

    String getStorageType() {
        return storageType
    }

    void setStorageType(String storageType) {
        this.storageType = storageType
    }

    Remote getRemote() {
        return remote
    }

    void setRemote(Remote remote) {
        this.remote = remote
    }
}
