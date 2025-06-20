package com.netgrif.application.engine.adapter.spring.plugin.service;

import java.io.Serializable;

public interface PluginService {
    Object call(String pluginCaseId, String entryPoint, String method, Serializable... args) throws IllegalArgumentException;
}
