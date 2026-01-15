package com.netgrif.application.engine.configuration;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GenericMapCache<V, E> implements Cache {
    private final String name;
    private final Class<V> valueType;
    private final Class<E> elementType;
    private final ConcurrentHashMap<String, V> map;

    public GenericMapCache(String name, Class<V> valueType, Class<E> elementType, int initialCapacity) {
        this.name = name;
        this.valueType = valueType;
        this.elementType = elementType;
        this.map = new ConcurrentHashMap<>(initialCapacity);
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
        if (value == null) {
            evict(key);
            return;
        }
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
    private V safeCast(Object value) {
        if (value == null) {
            return null;
        }

        if (valueType.isInstance(value)) {
            return (V) value;
        }

        // Check if the value is a list and the cache type is List<Element>
        if (value instanceof List && List.class.isAssignableFrom(valueType)) {
            List<?> list = (List<?>) value;

            // Check only if the list is non-empty
            if (!list.isEmpty()) {
                Object firstElement = list.getFirst();

                // Validate element type
                if (elementType != null && !elementType.isInstance(firstElement)) {
                    throw new ClassCastException(
                            String.format("Cannot cast list element of type %s to %s",
                                    firstElement.getClass().getName(),
                                    elementType.getName()
                            )
                    );
                }
            }

            return (V) list; // Safe cast to desired list type
        }

        throw new ClassCastException(
                String.format("Cannot cast value of type %s to %s",
                        value.getClass().getName(),
                        valueType.getName()
                )
        );
    }
}
