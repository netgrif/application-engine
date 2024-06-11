package com.netgrif.application.engine.validations;

import groovy.lang.Closure;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class ValidationRegistry {

    private final Map<String, Closure<Boolean>> validationsMap = new ConcurrentHashMap<>();

    public Closure<Boolean> addValidation(String name, Closure<Boolean> closure) {
        return validationsMap.put(name, closure);
    }

    public Closure<Boolean> getValidation(String name) {
        return validationsMap.get(name);
    }

    public List<String> getValidationNames() {
        return new ArrayList<>(validationsMap.keySet());
    }

    public Closure<Boolean> removeValidation(String name) {
        return validationsMap.remove(name);
    }
}
