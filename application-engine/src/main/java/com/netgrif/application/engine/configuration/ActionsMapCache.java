package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import groovy.lang.Closure;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ActionsMapCache extends GenericMapCache {

    public ActionsMapCache(String name, java.util.function.Supplier<Map<String, Closure>> mapFactory, IFieldActionsCacheService fieldActionsCacheService, IPetriNetService petriNetService) {
        super(name, Closure.class, mapFactory, fieldActionsCacheService, petriNetService);
    }

    @Override
    public ValueWrapper get(Object key) {
        String stringKey = String.valueOf(key);

        Object valueObject = map.get(stringKey);
        if (valueObject != null) {
            return new org.springframework.cache.support.SimpleValueWrapper(valueObject);
        }
        fieldActionsCacheService.reloadCachedFunctions(stringKey);
        return new org.springframework.cache.support.SimpleValueWrapper(map.get(stringKey));
    }

    public <T> T get(Object key, Class<T> type) {
        String stringKey = String.valueOf(key);
        Object valueObject = map.get(stringKey);

        if (valueObject != null) {
            return type.cast(valueObject);
        }

        fieldActionsCacheService.reloadCachedFunctions(stringKey);
        return type.cast(map.get(stringKey));

    }
}
