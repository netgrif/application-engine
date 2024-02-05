package com.netgrif.application.engine.files;

import com.netgrif.application.engine.files.interfaces.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StorageResolverService {

    @Autowired
    private List<IStorageService> storageServices;

    public IStorageService resolve(String type) {
        return storageServices.stream()
                .filter(service -> service.getType().equals(type))
                .collect(Collectors.toList()).get(0);
    }
}
