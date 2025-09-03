package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.util.Map;

@Slf4j
public abstract class GenericMapCache<V> implements Cache {
    private final String name;
    protected Class<?> valueType;
    private final java.util.function.Supplier<Map<String, V>> mapFactory;
    protected volatile Map<String, V> map;
    protected final IFieldActionsCacheService fieldActionsCacheService;

    protected final IPetriNetService petriNetService;

    public GenericMapCache(String name, Class<?> valueType, java.util.function.Supplier<Map<String, V>> mapFactory, IFieldActionsCacheService fieldActionsCacheService, IPetriNetService petriNetService) {
        this.name = name;
        this.valueType = valueType;
        this.mapFactory = mapFactory;
        this.map = mapFactory.get();
        this.fieldActionsCacheService = fieldActionsCacheService;
        this.petriNetService = petriNetService;
    }

    @Override public String getName() { return name; }

    @Override public Object getNativeCache() { return map; }

    @Override
    public <T> T get(Object key, java.util.concurrent.Callable<T> loader) {
        String stringKey = String.valueOf(key);
        Object present = map.get(stringKey);

        if (present != null) {
            return (T) present;
        }

        try {
            T computed = loader.call();
            if (computed != null) {
                map.put(stringKey, safeCast(computed));
            }
            return computed;
        } catch (Exception ex) {
            throw new org.springframework.cache.Cache.ValueRetrievalException(stringKey, loader, ex);
        }
    }

    @Override
    public void put(Object key, Object value) {
        map.put(String.valueOf(key), safeCast(value));
    }

    @Override
    public void evict(Object key) {
        map.remove(String.valueOf(key));
    }

    @Override
    public void clear() {
        this.map = mapFactory.get();
        log.info("{} cache cleared", this.getName());
    }

    protected V safeCast(Object object) {
        if (object == null) {
            return null;
        }

        if (!valueType.isInstance(object)) {
            throw new ClassCastException("Expected " + valueType.getName() + " but was " + object.getClass().getName());
        }

        return (V) object;
    }
}
