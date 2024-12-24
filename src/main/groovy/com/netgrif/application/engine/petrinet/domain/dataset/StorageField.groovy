package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.files.local.LocalStorageService

abstract class StorageField<T> extends Field<T> {

    public static final long serialVersionUID = -9172755427378929924L
    private Storage storage

    StorageField() {
        super()
    }

    String getStorageType() {
        if (storage == null) {
            return LocalStorageService.LOCAL_TYPE
        }
        return storage.getType()
    }

    void setStorageType(String storageType) {
        this.storage.setType(storageType)
    }

    Storage getStorage() {
        return storage
    }

    void setStorage(Storage storage) {
        this.storage = storage
    }
}
