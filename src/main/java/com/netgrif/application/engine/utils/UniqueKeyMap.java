package com.netgrif.application.engine.utils;

import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;


/**
 * Extends {@link LinkedHashMap} with a feature to throw {@link IllegalArgumentException} if the key already exists when
 * putting the key using {@link UniqueKeyMap#put(String, Object)}
 */
@NoArgsConstructor
public class UniqueKeyMap<K extends String, V> extends LinkedHashMap<K, V> {

    public UniqueKeyMap(UniqueKeyMap<K, V> m) {
        super(m);
    }

    @Override
    public V put(K key, V value) throws IllegalArgumentException {
        V previousValue = super.putIfAbsent(key, value);
        if (previousValue != null) {
            throw new IllegalArgumentException("Key is not unique: " + key);
        }
        return null;
    }

    @Override
    public UniqueKeyMap<K, V> clone() {
        return new UniqueKeyMap<>(this);
    }
}
