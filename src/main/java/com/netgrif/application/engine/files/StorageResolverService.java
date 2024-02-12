package com.netgrif.application.engine.files;

import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.StorageNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StorageResolverService {

    @Autowired(required = false)
    private List<IStorageService> storageServices;

    public IStorageService resolve(String type) {
        if (storageServices == null) {
            log.error("Storage services with interface IStorageService not found.");
            throw new StorageNotFoundException("Remote Storage not available.");
        }
        IStorageService storageService = storageServices.stream().filter(service -> service.getType().equals(type)).collect(Collectors.toList()).get(0);
        if (storageService == null) {
            throw new StorageNotFoundException("Storage Service with type: " + type + " not available.");
        }
        return storageService;
    }
}
