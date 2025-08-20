package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public abstract class StorageField<T> extends Field<T> {

    @Serial
    private static final long serialVersionUID = -9172755427378929924L;
    private Storage storage;

    public StorageField() {
        super();
    }

    public String getStorageType() {
        return storage.getType();
    }

    public void setStorageType(String storageType) {
        this.storage.setType(storageType);
    }
}
