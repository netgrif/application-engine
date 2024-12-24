package com.netgrif.application.engine.petrinet.domain.dataset.factory;

import com.netgrif.application.engine.files.IStorageResolverService;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.petrinet.domain.dataset.Storage;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class StorageFactory {

    public static Storage createStorage(Data data, IStorageResolverService storageResolverService, String defaultStorageType) {
        if (data == null) return null;
        Storage storage;
        String storageType = (data.getStorage() == null || data.getStorage().getType() == null) ? defaultStorageType : data.getStorage().getType().toLowerCase();

        Set<String> storageTypes = storageResolverService.availableStorageTypes();
        if (storageTypes.contains(storageType)) {
            IStorageService storageService = storageResolverService.resolve(storageType);
            storage = storageService.createStorage(data);
        } else {
            log.warn("Storage of type [" + storageType + "] is not enabled. Fallback to " + defaultStorageType);
            storage = new Storage(defaultStorageType);
        }

        return storage;
    }
}
