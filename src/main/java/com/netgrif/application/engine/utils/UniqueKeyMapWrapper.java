package com.netgrif.application.engine.utils;

import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;


/**
 * Extends {@link LinkedHashMap} with a feature to throw {@link IllegalArgumentException} if the key already exists when
 * putting the key using {@link UniqueKeyMapWrapper#put(String, Object)}
 */
public class UniqueKeyMapWrapper<V> {

    @Getter
    private final LinkedHashMap<String, V> map;

    public UniqueKeyMapWrapper() {
        this.map = new LinkedHashMap<>();
    }

    public UniqueKeyMapWrapper(UniqueKeyMapWrapper<V> m) {
        this.map = new LinkedHashMap<>(m.map);
    }

    public V put(String key, V value) throws IllegalArgumentException {
        V previousValue = putIfAbsent(key, value);
        if (previousValue != null) {
            throw new IllegalArgumentException("Key is not unique: " + key);
        }
        return null;
    }

    public boolean containsKey(String key) {
        return this.map.containsKey(key);
    }

    public V get(String key) {
        return this.map.get(key);
    }

    public V getOrDefault(String key, V defaultValue) {
        return this.map.getOrDefault(key, defaultValue);
    }

    public Collection<V> values() {
        return this.map.values();
    }

    public void forEach(BiConsumer<String, V> action) {
        this.map.forEach(action);
    }

    public Set<Map.Entry<String, V>> entrySet() {
        return this.map.entrySet();
    }

    public void remove(String key) {
        this.map.remove(key);
    }

    public int size() {
        return this.map.size();
    }

    @Override
    public UniqueKeyMapWrapper<V> clone() {
        return new UniqueKeyMapWrapper<>(this);
    }

    private V putIfAbsent(String key, V value) {
        return this.map.putIfAbsent(key, value);
    }
}
