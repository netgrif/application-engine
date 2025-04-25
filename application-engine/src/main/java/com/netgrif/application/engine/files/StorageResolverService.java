package com.netgrif.application.engine.files;

import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.StorageNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StorageResolverService implements IStorageResolverService {

    private Map<String, IStorageService> storageServices;

    @Autowired
    private void setStorageServices(List<IStorageService> storageServices) {
        this.storageServices = storageServices.stream().collect(Collectors.toMap(IStorageService::getType, Function.identity()));
    }

    @Override
    public IStorageService resolve(String type) {
        if (storageServices == null) {
            log.error("Storage services with interface IStorageService not found.");
            throw new StorageNotFoundException("Remote Storage not available.");
        }
        if (storageServices.containsKey(type)) {
            return storageServices.get(type);
        }
        throw new StorageNotFoundException("Storage Service with type: " + type + " not available.");

    }

    @Override
    public Set<String> availableStorageTypes() {
        return storageServices.keySet();
    }

}
