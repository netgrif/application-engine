package com.netgrif.application.engine.resource.service;

import com.netgrif.application.engine.resource.domain.NetgrifResourceLoader;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class NetgrifResourceLoaderProcessor implements ResourceLoaderAware, ProtocolResolver {

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if(DefaultResourceLoader.class.isAssignableFrom(resourceLoader.getClass())) {
            ((DefaultResourceLoader)resourceLoader).addProtocolResolver(this);
        } else {
            System.out.println("Could not assign protocol loader.");
        }
    }

    @Override
    public Resource resolve(String location, ResourceLoader resourceLoader) {
        if(location.startsWith(NetgrifResourceLoader.RESOURCE_PREFIX)) {
            NetgrifResourceLoader loader = new NetgrifResourceLoader(resourceLoader);
            return loader.getResource(location);
        }
        return null;
    }
}
