package com.netgrif.application.engine.integration.plugins.config;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.JarPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PluginManagerConfiguration {

    @Bean
    public JarPluginManager pluginManager() {
        return new JarPluginManager();
    }
}
