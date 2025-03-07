package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.pluginlibrary.core.RegistrationRequest;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface IPluginService {
    String registerOrActivate(RegistrationRequest request);

    String unregister(String identifier) throws IllegalArgumentException;

    Object call(String url, int port, String entryPoint, String method, Serializable... args) throws IllegalArgumentException;

    String deactivate(String identifier) throws IllegalArgumentException;

    List<Case> findAll();

    Optional<Case> findByIdentifier(String identifier);
}
