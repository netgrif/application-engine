//package com.netgrif.workflow.pluginmanager.config;
//
//import org.pf4j.AbstractPluginManager;
//import org.pf4j.JarPluginLoader;
//import org.pf4j.JarPluginManager;
//import org.pf4j.PluginManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class PluginManagerConfiguration {
//
//
//    @Bean
//    public AbstractPluginManager pluginManager() {
//        return new JarPluginManager();
//    }
//
//    @Bean
//    public JarPluginLoader pluginLoader(PluginManager pluginManager) {
//        return (JarPluginLoader) ((AbstractPluginManager) pluginManager).getPluginLoader();
//    }
//}
