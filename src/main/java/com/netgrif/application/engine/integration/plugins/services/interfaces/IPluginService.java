package com.netgrif.application.engine.integration.plugins.services.interfaces;

import org.pf4j.Plugin;
import wrapper.NaePlugin;

import java.util.List;

public interface IPluginService {

    NaePlugin getPlugin(String pluginId);

    List<NaePlugin> getAllPlugin();

    <T> T call(String pluginId, String extensionName, String method, List<Object> argumentValues, Class<T> returnType);
}
