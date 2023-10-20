package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.application.engine.integration.plugins.domain.Plugin;

public interface IPluginService {
    void register(Plugin plugin);

    void unregister(String identifier);
}
