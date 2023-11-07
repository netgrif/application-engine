package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.application.engine.integration.plugins.domain.Plugin;

import java.io.Serializable;

public interface IPluginService {
    void register(Plugin plugin);

    Object call(String pluginId, String entryPoint, String method, Serializable... args);

    void deactivate(String identifier);
}
