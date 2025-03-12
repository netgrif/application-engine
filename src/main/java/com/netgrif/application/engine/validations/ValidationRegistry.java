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
        if (validationsMap.containsKey(name)) {
            throw new IllegalArgumentException("Validation with name " + name + " already exists.");
        }
        return validationsMap.put(name, closure);
    }

    public Closure<Boolean> getValidation(String name) {
        return validationsMap.get(name);
    }

    public Closure<Boolean> removeValidation(String name) {
        return validationsMap.remove(name);
    }

    public void removeAllValidations() { validationsMap.clear(); }
}
