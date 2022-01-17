package com.netgrif.workflow.pluginmanager.config;

import org.pf4j.JarPluginLoader;
import org.pf4j.JarPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginManagerConfiguration {


    @Bean
    public JarPluginManager pluginManager() {
        return new JarPluginManager();
    }

    @Bean
    public JarPluginLoader pluginLoader(JarPluginManager pluginManager) {
        return (JarPluginLoader) pluginManager.getPluginLoader();
    }
}
