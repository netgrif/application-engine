package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.elastic.service.executors.MaxSizeHashMap;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
public class GenericMapCache<V> implements Cache {
    private final String name;
    private final Class<V> valueType;
    private final Map<String, V> map;

    public GenericMapCache(String name, Class<V> valueType, int cacheSize) {
        this.name = name;
        this.valueType = valueType;
        this.map = Collections.synchronizedMap(new MaxSizeHashMap<>(cacheSize));
    }

    @Override public @NotNull String getName() { return name; }

    @Override public @NotNull Object getNativeCache() { return Map.copyOf(map); }

    @Override
    public <T> T get(Object key, Callable<T> loader) {
        final String stringKey = String.valueOf(key);
        try {
            V value = map.computeIfAbsent(stringKey, cacheValue -> {
                try {
                    T computed = loader.call();
                    if (computed == null) return null;
                    return safeCast(computed);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return (T) value;
        } catch (RuntimeException e) {
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;
            throw new Cache.ValueRetrievalException(stringKey, loader, cause);
        }
    }

    @Override
    public ValueWrapper get(Object key) {
        String stringKey = String.valueOf(key);
        Object valueObject = map.get(stringKey);
        return valueObject != null ? new SimpleValueWrapper(valueObject) : null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        String stringKey = String.valueOf(key);
        Object valueObject = map.get(stringKey);
        return valueObject != null ? type.cast(valueObject) : null;
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
        map.clear();
    }

    @SuppressWarnings("unchecked")
    private V safeCast(Object object) {
        if (object == null) {
            return null;
        }

        if (!valueType.isInstance(object)) {
            throw new ClassCastException("Expected " + valueType.getName() + " but was " + object.getClass().getName());
        }

        return (V) object;
    }
}
