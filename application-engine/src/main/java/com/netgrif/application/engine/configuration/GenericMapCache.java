package com.netgrif.application.engine.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.util.Map;

@Slf4j
public class GenericMapCache<V> implements Cache {
    private final String name;
    protected Class<?> valueType;
    private final java.util.function.Supplier<Map<String, V>> mapFactory;
    private volatile Map<String, V> map;

    public GenericMapCache(String name, Class<?> valueType, java.util.function.Supplier<Map<String, V>> mapFactory) {
        this.name = name;
        this.valueType = valueType;
        this.mapFactory = mapFactory;
        this.map = mapFactory.get();
    }

    @Override public String getName() { return name; }
    @Override public Object getNativeCache() { return map; }

    @Override public ValueWrapper get(Object key) {
        Object valueObject = map.get(String.valueOf(key));
        return (valueObject == null) ? null : new org.springframework.cache.support.SimpleValueWrapper(valueObject);
    }

    @Override public <T> T get(Object key, Class<T> type) {
        Object valueObject = map.get(String.valueOf(key));
        return (valueObject == null) ? null : type.cast(valueObject);
    }

    @Override public <T> T get(Object key, java.util.concurrent.Callable<T> loader) {
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

    @Override public void put(Object key, Object value) {
        map.put(String.valueOf(key), safeCast(value));
    }

    @Override public void evict(Object key) {
        map.remove(String.valueOf(key));
    }

    @Override public void clear() {
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
