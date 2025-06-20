package com.netgrif.application.engine.adapter.spring.plugin.config;

import com.netgrif.application.engine.objects.plugin.domain.EntryPoint;

import java.util.Map;

public interface PluginRegistrationConfiguration {
    String getPluginName();
    String getVersion();
    Map<String, EntryPoint> getEntryPoints();
}