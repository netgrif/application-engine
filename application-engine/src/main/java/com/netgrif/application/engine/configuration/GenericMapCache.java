package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.security.core.parameters.P;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public abstract class GenericMapCache<V> implements Cache {
    private final String name;
    protected Class<?> valueType;
    private final java.util.function.Supplier<Map<String, V>> mapFactory;
    private final AtomicReference<Map<String, V>> atomicMapRef;

    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();
    protected final IFieldActionsCacheService fieldActionsCacheService;

    protected final IPetriNetService petriNetService;

    public GenericMapCache(String name, Class<?> valueType, java.util.function.Supplier<Map<String, V>> mapFactory, IFieldActionsCacheService fieldActionsCacheService, IPetriNetService petriNetService) {
        this.name = name;
        this.valueType = valueType;
        this.mapFactory = mapFactory;
        this.atomicMapRef = new AtomicReference<>(mapFactory.get());
        this.fieldActionsCacheService = fieldActionsCacheService;
        this.petriNetService = petriNetService;
    }

    @Override public String getName() { return name; }

    @Override public Object getNativeCache() { return Map.copyOf(map()); }

    @Override
    @SuppressWarnings("unchecked")
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
            throw new ValueRetrievalException(stringKey, loader, ex);
        } finally {
            locks.remove(stringKey, lock);
        }
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
        log.info("{} cache cleared", this.getName());
    }

    protected Map<String, V> map() {
        return atomicMapRef.get();
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
