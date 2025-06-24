package com.netgrif.application.engine.adapter.spring.plugin.service;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

public interface PluginService {
    Object call(String pluginCaseId, String entryPoint, String method, Serializable... args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;
}
