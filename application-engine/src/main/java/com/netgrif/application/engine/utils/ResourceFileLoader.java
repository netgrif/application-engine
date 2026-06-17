package com.netgrif.application.engine.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ResourceFileLoader {

    private static ResourceLoader resourceLoader;

    public static File loadResourceFile(String path) throws IOException {
        return resourceLoader.getResource(path).getFile();
    }

    @Autowired
    public void setResourceLoader(@Qualifier("webApplicationContext") ResourceLoader resourceLoader) {
        ResourceFileLoader.resourceLoader = resourceLoader;
    }
}