package com.netgrif.application.engine.objects.utils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Minimal thread-safe in-memory cache whose entries expire after a fixed time to live.
 * <p>
 * Intended for small, rarely changing lookups (e.g. process roles, authorities) that are read many times per request.
 * Writers should call {@link #invalidate(Object)} or {@link #clear()}; the time to live only bounds how long a change
 * made outside of the owning service stays invisible.
 * </p>
 * <p>
 * {@code null} loader results are never cached. When the cache reaches its maximum size it is cleared as a whole.
 * </p>
 *
 * @param <K> key type
 * @param <V> value type
 */
public class TtlCache<K, V> {

    private record Entry<V>(V value, long expiresAtNanos) {
    }

    private final Map<K, Entry<V>> entries = new ConcurrentHashMap<>();
    private final long ttlNanos;
    private final int maxSize;

    public TtlCache(Duration ttl, int maxSize) {
        this.ttlNanos = ttl.toNanos();
        this.maxSize = maxSize;
    }

    /**
     * Returns the cached value for the key, or loads it with the given loader when it is missing or expired.
     * The loader may return {@code null}, which is returned to the caller but not cached.
     */
    public V get(K key, Function<K, V> loader) {
        long now = System.nanoTime();
        Entry<V> entry = entries.get(key);
        if (entry != null && now - entry.expiresAtNanos() < 0) {
            return entry.value();
        }
        V value = loader.apply(key);
        if (value == null) {
            entries.remove(key);
            return null;
        }
        if (entries.size() >= maxSize) {
            entries.clear();
        }
        entries.put(key, new Entry<>(value, System.nanoTime() + ttlNanos));
        return value;
    }

    public void invalidate(K key) {
        entries.remove(key);
    }

    public void clear() {
        entries.clear();
    }
}
