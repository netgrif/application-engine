package com.netgrif.application.engine.resource.domain;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class NetgrifResourceLoader implements ResourceLoader {

    public static final String RESOURCE_PREFIX = "resource://";

    private final ResourceLoader delegate;

    public NetgrifResourceLoader(ResourceLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public Resource getResource(String location) {
        if(location.startsWith(RESOURCE_PREFIX)) {
            String path = location.substring(RESOURCE_PREFIX.length());
            NetgrifResource resource = new NetgrifResource();
            return resource.getResource("resources/" + path);
        }
        return delegate.getResource(location);
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.delegate.getClassLoader();
    }
}
