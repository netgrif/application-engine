package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.core.workflow.domain.Case;
import com.netgrif.pluginlibrary.core.domain.Plugin;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface PluginService {
    String registerOrActivate(Plugin plugin);

    String unregister(String identifier) throws IllegalArgumentException;

    Object call(String url, int port, String entryPoint, String method, Serializable... args) throws IllegalArgumentException;

    String deactivate(String identifier) throws IllegalArgumentException;

    List<Case> findAll();

    Optional<Case> findByIdentifier(String identifier);
}
