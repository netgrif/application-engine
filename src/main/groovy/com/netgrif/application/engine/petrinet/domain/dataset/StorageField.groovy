package com.netgrif.application.engine.petrinet.domain.dataset

abstract class StorageField<T> extends Field<T> {
    static final long serialVersionUID = -9172755427378929924L
    private Storage storage

    StorageField() {
        super()
    }

    String getStorageType() {
        if (storage == null) {
            return StorageType.LOCAL
        }
        return storage.getType().name()
    }

    void setStorageType(StorageType storageType) {
        this.storage.setType(storageType)
    }

    Storage getStorage() {
        return storage
    }

    void setStorage(Storage storage) {
        this.storage = storage
    }
}
