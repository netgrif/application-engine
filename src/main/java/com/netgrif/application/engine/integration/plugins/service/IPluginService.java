package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.application.engine.integration.plugins.domain.Plugin;

import java.io.Serializable;
import java.util.List;

public interface IPluginService {
    String registerOrActivate(Plugin plugin);

    Object call(String pluginId, String entryPoint, String method, Serializable... args) throws IllegalArgumentException;

    String deactivate(String identifier) throws IllegalArgumentException;

    List<Plugin> findAll();
}
