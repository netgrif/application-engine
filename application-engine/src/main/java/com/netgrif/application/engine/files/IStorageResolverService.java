package com.netgrif.application.engine.files;

import com.netgrif.application.engine.files.interfaces.IStorageService;

import java.util.Set;

public interface IStorageResolverService {
    IStorageService resolve(String type);

    Set<String> availableStorageTypes();
}
