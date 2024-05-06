package com.netgrif.application.engine.integration.plugins.domain;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class EntryPoint {
    private String name;
    private Map<String, Method> methods;

    public EntryPoint() {
        this.methods = new LinkedHashMap<>();
    }
}
