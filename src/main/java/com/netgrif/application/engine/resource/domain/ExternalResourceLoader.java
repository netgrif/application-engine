package com.netgrif.application.engine.resource.domain;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

public class ExternalResourceLoader implements ResourceLoader {

    public static final String RESOURCES_FOLDER = "resources";
    public static final String[] RESOURCE_PREFIXES = new String[]{
            "resource:",
            "nae:",
            "nae-resource:",
            "nr:"
    };

    public static final String DEFAULT_RESOURCE_PREFIX = RESOURCE_PREFIXES[0];

    public static final String NAE_RESOURCE_PREFIX = RESOURCE_PREFIXES[1];

    public static final String NAE_RESOURCE_RESOURCE_PREFIX = RESOURCE_PREFIXES[2];

    public static final String NR_RESOURCE_PREFIX = RESOURCE_PREFIXES[3];

    private final ResourceLoader delegate;

    public ExternalResourceLoader(ResourceLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public Resource getResource(String location) {
        if (location.isBlank()) return delegate.getResource(location);
        Optional<String> detectedPrefix = getExternalResourcePrefix(location);
        return detectedPrefix.isPresent() ? getResourceWithPrefix(location, detectedPrefix.get()) : delegate.getResource(location);
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.delegate.getClassLoader();
    }

    /**
     * Get detected resource prefix if the location is an external resource.
     *
     * @param location Resource location
     * @return Optional of resource prefix. If the location is not external resource, empty Optional is returned.
     */
    public static Optional<String> getExternalResourcePrefix(String location) {
        if (location == null || location.isBlank()) return Optional.empty();
        return Arrays.stream(RESOURCE_PREFIXES).filter(location.toLowerCase()::startsWith).findFirst();
    }

    private Resource getResourceWithPrefix(String location, String prefix) {
        String path = location.substring(prefix.length());
        ExternalResource resource = new ExternalResource();
        return resource.getResource(RESOURCES_FOLDER + File.pathSeparator + path);
    }
}
