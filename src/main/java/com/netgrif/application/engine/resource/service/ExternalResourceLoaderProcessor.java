package com.netgrif.application.engine.resource.service;

import com.netgrif.application.engine.resource.domain.ExternalResourceLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalResourceLoaderProcessor implements ResourceLoaderAware, ProtocolResolver {

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if(DefaultResourceLoader.class.isAssignableFrom(resourceLoader.getClass())) {
            ((DefaultResourceLoader)resourceLoader).addProtocolResolver(this);
        } else {
            log.error("Could not assign protocol loader.");
        }
    }

    @Override
    public Resource resolve(String location, ResourceLoader resourceLoader) {
        if(location.startsWith(ExternalResourceLoader.RESOURCE_PREFIX) ||
                location.startsWith(ExternalResourceLoader.NAE_PREFIX) ||
                location.startsWith(ExternalResourceLoader.NAE_RESOURCE_PREFIX) ||
                location.startsWith(ExternalResourceLoader.NR_PREFIX)) {
            ExternalResourceLoader loader = new ExternalResourceLoader(resourceLoader);
            return loader.getResource(location);
        }
        return null;
    }
}
