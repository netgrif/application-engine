package com.netgrif.application.engine.adapter.spring.plugin.config;

public interface PluginRegistrationConfiguration {
    String getPluginName();
    String getVersion();
    Object getEntryPoints();
}