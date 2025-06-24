package com.netgrif.application.engine.objects.plugin.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain class for entry points. Entry points are beans annotated with {@link com.netgrif.plugin.core.annotations.EntryPoint}
 * annotation. These beans contain methods, that can be run from server where the plugin is registered.
 * */
@Data
public class EntryPoint implements Serializable {

    @Serial
    private static final long serialVersionUID = -4312516499873834830L;

    private String name;

    private String pluginName;

    /**
     * Map of {@link Method}, key of the map is equivalent to {@link Method#getName()}
     * */
    private Map<String, Method> methods;

    public EntryPoint() {
        methods = new HashMap<>();
    }

    @Builder
    public EntryPoint(String name, Map<String, Method> methods, String pluginName) {
        this.name = name;
        this.methods = methods;
        this.pluginName = pluginName;
    }
}
