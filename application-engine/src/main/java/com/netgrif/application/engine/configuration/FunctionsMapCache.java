package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.objects.petrinet.domain.Function;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.domain.CachedFunction;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class FunctionsMapCache extends GenericMapCache {

    public FunctionsMapCache(String name, java.util.function.Supplier<Map<String, CachedFunction>> mapFactory, IFieldActionsCacheService fieldActionsCacheService, IPetriNetService petriNetService) {
        super(name, CachedFunction.class, mapFactory, fieldActionsCacheService, petriNetService);
    }

    @Override
    public ValueWrapper get(Object key) {
        String stringKey = String.valueOf(key);

        Object valueObject = map.get(stringKey);
        if (valueObject != null) {
            return new org.springframework.cache.support.SimpleValueWrapper(valueObject);
        }

        Function function = petriNetService.findByFunctionId(stringKey);
        if (function != null) {
            map.put(stringKey, function);
            return new org.springframework.cache.support.SimpleValueWrapper(function);
        } else {
            return new org.springframework.cache.support.SimpleValueWrapper(null);
        }
    }

    public <T> T get(Object key, Class<T> type) {
        String stringKey = String.valueOf(key);
        Object valueObject = map.get(stringKey);

        if (valueObject != null) {
            return type.cast(valueObject);
        }

        Function function = petriNetService.findByFunctionId(stringKey);
        if (function != null) {
            map.put(stringKey, function);
            return type.cast(function);
        } else {
            return type.cast(null);
        }
    }
}
