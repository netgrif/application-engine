package com.netgrif.application.engine.petrinet.domain.dataset.factory;

import com.netgrif.application.engine.files.minio.MinIoProperties;
import com.netgrif.application.engine.files.throwable.StorageNotEnabledException;
import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.petrinet.domain.dataset.*;

import static com.netgrif.application.engine.files.minio.MinIoStorageService.getBucketOrDefault;

public class StorageFactory {

    public static Storage createStorage(Data data, String defaultStorageType, MinIoProperties minIoProperties) {
        Storage storage;
        StorageType storageType = StorageType.valueOf((data.getStorage() == null || data.getStorage().getType() == null) ? defaultStorageType : data.getStorage().getType().toUpperCase());
        switch (storageType) {
            case MINIO:
                storage = new MinIoStorage();
                if (!minIoProperties.isEnabled()) {
                    throw new StorageNotEnabledException("Storage of type [" + StorageType.MINIO + "] is not enabled.");
                }
                if (data.getStorage().getHost() != null) {
                    storage.setHost(data.getStorage().getHost());
                }
                if (data.getStorage().getBucket() != null) {
                    ((MinIoStorage) storage).setBucket(getBucketOrDefault(data.getStorage().getBucket()));
                }
                break;
            default:
                storage = new Storage(StorageType.valueOf(defaultStorageType));
                break;
        }
        return storage;
    }
}
