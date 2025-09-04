package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.objects.petrinet.domain.Function;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.domain.CachedFunction;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.SimpleValueWrapper;

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

        Object valueObject = map().get(stringKey);
        if (valueObject != null) {
            return new SimpleValueWrapper(List.copyOf((List<CachedFunction>) valueObject));
        }
        PetriNet petriNet = petriNetService.getPetriNet(stringKey);
        fieldActionsCacheService.cachePetriNetFunctions(petriNet);
        return new SimpleValueWrapper(List.copyOf((List<CachedFunction>) map().get(stringKey)));
    }

    public <T> T get(Object key, Class<T> type) {
        String stringKey = String.valueOf(key);
        Object valueObject = map().get(stringKey);

        if (valueObject != null) {
            return type.cast(List.copyOf((List<CachedFunction>) valueObject));
        }

        PetriNet petriNet = petriNetService.getPetriNet(stringKey);
        fieldActionsCacheService.cachePetriNetFunctions(petriNet);
        return type.cast(List.copyOf((List<CachedFunction>) map().get(stringKey)));

    }

    @Override
    public void put(Object key, Object value) {
        String k = String.valueOf(key);
        map().put(k, List.copyOf((List<CachedFunction>) value));
    }
}
