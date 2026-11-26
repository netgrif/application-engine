package com.netgrif.application.engine.objects.utils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Minimal thread-safe in-memory cache whose entries expire after a fixed time to live.
 * <p>
 * Intended for small, rarely changing lookups (e.g. process roles, authorities) that are read many times per request.
 * Writers should call {@link #invalidate(Object)} or {@link #clear()} <em>after</em> persisting the change; the time to
 * live only bounds how long a change made outside of the owning service stays invisible. A value whose load overlapped
 * an invalidation is returned to its caller but not cached.
 * </p>
 * <p>
 * {@code null} loader results are never cached. When the cache reaches its maximum size it is cleared as a whole.
 * A time to live or maximum size of zero or less disables caching: the loader is called on every {@link #get}.
 * </p>
 *
 * @param <K> key type
 * @param <V> value type
 */
public class TtlCache<K, V> {

    /**
     * Cached entries by key. Each entry holds the value together with its absolute expiration time; expired entries
     * are not evicted eagerly, they are replaced on the next {@link #get} for their key.
     */
    private final Map<K, Entry<V>> entries = new ConcurrentHashMap<>();

    /**
     * Time to live of every entry in nanoseconds, measured from the moment the value is published to the cache.
     * A value of zero or less disables caching.
     */
    private final long ttlNanos;

    /**
     * Maximum number of entries kept in the cache. When it is reached, the whole cache is cleared before the next
     * value is published. A value of zero or less disables caching.
     */
    private final int maxSize;

    /**
     * Guards publication of loaded values together with invalidation, so that a load which started before an
     * invalidation can never publish its (possibly stale) result after it.
     */
    private final Object publishLock = new Object();

    /**
     * Incremented, under {@link #publishLock}, by every invalidation. A load publishes its value only if the
     * generation is still the one it observed before it started loading.
     */
    private volatile long generation;

    public TtlCache(Duration ttl, int maxSize) {
        this.ttlNanos = ttl.toNanos();
        this.maxSize = maxSize;
    }

    /**
     * Returns the cached value for the key, or loads it with the given loader when it is missing or expired.
     * The loader may return {@code null}, which is returned to the caller but not cached.
     */
    public V get(K key, Function<K, V> loader) {
        if (ttlNanos <= 0 || maxSize <= 0) {
            return loader.apply(key);
        }
        long now = System.nanoTime();
        Entry<V> entry = entries.get(key);
        if (entry != null && now - entry.expiresAtNanos() < 0) {
            return entry.value();
        }
        long generationBeforeLoad = generation;
        V value = loader.apply(key);
        if (value == null) {
            entries.remove(key);
            return null;
        }
        synchronized (publishLock) {
            if (generation != generationBeforeLoad) {
                // invalidated while loading: the value may predate the change that caused the invalidation
                return value;
            }
            if (entries.size() >= maxSize) {
                entries.clear();
            }
            entries.put(key, new Entry<>(value, System.nanoTime() + ttlNanos));
        }
        return value;
    }

    public void invalidate(K key) {
        synchronized (publishLock) {
            generation++;
            entries.remove(key);
        }
    }

    public void clear() {
        synchronized (publishLock) {
            generation++;
            entries.clear();
        }
    }

    private record Entry<V>(V value, long expiresAtNanos) {
    }
}
