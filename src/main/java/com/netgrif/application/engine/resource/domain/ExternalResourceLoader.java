package com.netgrif.application.engine.resource.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class ExternalResourceLoader implements ResourceLoader {

    public static final String RESOURCE_PREFIX = "resource:";

    public static final String NAE_PREFIX = "nae:";

    public static final String NAE_RESOURCE_PREFIX = "nae-resource:";

    public static final String NR_PREFIX = "nr:";

    private final ResourceLoader delegate;

    public ExternalResourceLoader(ResourceLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public Resource getResource(String location) {
        if(location.startsWith(RESOURCE_PREFIX)) {
            return substringAndGetResource(location, RESOURCE_PREFIX);
        } else if(location.startsWith(NAE_PREFIX)) {
            return substringAndGetResource(location, NAE_PREFIX);
        } else if(location.startsWith(NAE_RESOURCE_PREFIX)) {
            return substringAndGetResource(location, NAE_RESOURCE_PREFIX);
        } else if(location.startsWith(NR_PREFIX)) {
            return substringAndGetResource(location, NR_PREFIX);
        }
        return delegate.getResource(location);
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.delegate.getClassLoader();
    }

    private Resource substringAndGetResource(String location, String prefix) {
        String path = location.substring(prefix.length());
        ExternalResource resource = new ExternalResource();
        return resource.getResource(StringUtils.chop(prefix)+ "/" + path);
    }
}
