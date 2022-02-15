package com.netgrif.application.engine.integration.plugins.security;

import com.netgrif.application.engine.integration.plugins.config.PluginProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.Policy;

@Slf4j
@Configuration
public class PluginSecurityConfiguration {

    @Autowired
    private PluginProperties pluginProperties;

    @PostConstruct
    protected void init() {
        if (pluginProperties.getPermissions() != null && pluginProperties.getPermissions().size() > 0)
            Policy.setPolicy(new PluginSecurityPolicy(pluginProperties.getPermissions()));
    }
}
