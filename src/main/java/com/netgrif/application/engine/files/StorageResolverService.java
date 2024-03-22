package com.netgrif.application.engine.files;

import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.StorageNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StorageResolverService {

    private List<IStorageService> storageServices;

    @Autowired
    private void setStorageServices(List<IStorageService> storageServices) {
        this.storageServices = storageServices;
    }

    public IStorageService resolve(StorageType type) {
        if (storageServices == null) {
            log.error("Storage services with interface IStorageService not found.");
            throw new StorageNotFoundException("Remote Storage not available.");
        }
        Optional<IStorageService> storageService = storageServices.stream().filter(service -> service.getType().equals(type)).collect(Collectors.toList()).stream().findFirst();
        if (storageService.isPresent()) {
            return storageService.get();
        }
        throw new StorageNotFoundException("Storage Service with type: " + type + " not available.");

    }
}
