package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.domain.CachedFunction;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class FunctionsNamespaceMapCache extends GenericMapCache {

    public FunctionsNamespaceMapCache(String name, java.util.function.Supplier<Map<String, List<CachedFunction>>> mapFactory, IFieldActionsCacheService fieldActionsCacheService, IPetriNetService petriNetService) {
        super(name, List.class, mapFactory, fieldActionsCacheService, petriNetService);
    }

    @Override
    public ValueWrapper get(Object key) {
        String stringKey = String.valueOf(key);

        Object valueObject = map.get(stringKey);
        if (valueObject != null) {
            return new org.springframework.cache.support.SimpleValueWrapper(valueObject);
        }
        PetriNet petriNet = petriNetService.getPetriNet(stringKey);
        fieldActionsCacheService.cachePetriNetFunctions(petriNet);
        return new org.springframework.cache.support.SimpleValueWrapper(map.get(stringKey));
    }

    public <T> T get(Object key, Class<T> type) {
        String stringKey = String.valueOf(key);
        Object valueObject = map.get(stringKey);

        if (valueObject != null) {
            return type.cast(valueObject);
        }

        PetriNet petriNet = petriNetService.getPetriNet(stringKey);
        fieldActionsCacheService.cachePetriNetFunctions(petriNet);
        return type.cast(map.get(stringKey));

    }
}
