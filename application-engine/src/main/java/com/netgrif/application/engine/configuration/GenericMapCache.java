package com.netgrif.application.engine.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class GenericMapCache<V> implements Cache {
    private final String name;
    private final Class<V> valueType;
    private final java.util.function.Supplier<Map<String, V>> mapFactory;
    private final AtomicReference<Map<String, V>> atomicMapRef;

    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public GenericMapCache(String name, Class<V> valueType, java.util.function.Supplier<Map<String, V>> mapFactory) {
        this.name = name;
        this.valueType = valueType;
        this.mapFactory = mapFactory;
        this.atomicMapRef = new AtomicReference<>(mapFactory.get());
    }

    @Override public String getName() { return name; }

    @Override public Object getNativeCache() { return Map.copyOf(map()); }

    @Override
    public <T> T get(Object key, Callable<T> loader) {
        final String stringKey = String.valueOf(key);

        Object mapValue = map().get(stringKey);
        if (mapValue != null) {
            return (T) mapValue;
        }

        Object lock = locks.computeIfAbsent(stringKey, lockKey -> new Object());
        try {
            synchronized (lock) {
                Object mapLockValue = map().get(stringKey);
                if (mapLockValue != null) {
                    return (T) mapLockValue;
                }

                T computed = loader.call();
                if (computed == null) {
                    return null;
                }

                V value = safeCast(computed);
                map().put(stringKey, value);
                return (T) value;
            }
        } catch (Exception ex) {
            throw new Cache.ValueRetrievalException(stringKey, loader, ex);
        } finally {
            locks.remove(stringKey, lock);
        }
    }

    @Override
    public ValueWrapper get(Object key) {
        String stringKey = String.valueOf(key);
        Object valueObject = map().get(stringKey);
        return valueObject != null ? new SimpleValueWrapper(valueObject) : null;
    }

    @Override
    public synchronized <T> T get(Object key, Class<T> type) {
        String stringKey = String.valueOf(key);
        Object valueObject = map().get(stringKey);
        return valueObject != null ? type.cast(valueObject) : null;
    }

    @Override
    public synchronized void put(Object key, Object value) {
        map().put(String.valueOf(key), safeCast(value));
    }

    @Override
    public synchronized void evict(Object key) {
        map().remove(String.valueOf(key));
    }

    @Override
    public synchronized void clear() {
        this.atomicMapRef.set(mapFactory.get());
    }

    private Map<String, V> map() {
        return atomicMapRef.get();
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
