package com.netgrif.application.engine.integration.plugins.services.interfaces;

import org.pf4j.Plugin;

import java.util.List;

public interface IPluginService {

    Plugin getPlugin(String pluginId);

    <T> T call(String pluginId, String extensionName, String method, List<Object> argumentValues, Class<T> returnType);
}
