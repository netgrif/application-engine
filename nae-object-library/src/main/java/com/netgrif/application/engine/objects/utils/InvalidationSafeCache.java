package com.netgrif.application.engine.objects.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

import java.time.Duration;
import java.util.function.Function;

/**
 * In-memory cache for small, rarely changing lookups (e.g. process roles, authorities) that are read many times per
 * request. Storage, expiry and size-based eviction are done by Caffeine; this class only adds the guarantee that a
 * value which was loaded while an invalidation happened is never cached.
 * <p>
 * Why not plain {@code Cache.get(key, loader)}: Caffeine's {@code invalidateAll()} does not affect a load that is
 * already running, so that load would still publish its (possibly stale) value after the invalidation. In addition,
 * a loader running inside {@code get(key, loader)} must not touch the same cache (it fails with "Recursive update").
 * Here the loader runs outside of any lock and outside of Caffeine, and only the publication of its result is
 * coordinated with invalidation.
 * </p>
 * <p>
 * Writers should call {@link #invalidate(Object)} or {@link #clear()} <em>after</em> persisting the change. The time
 * to live bounds how long a change made outside of the owning service stays invisible. As with any check-then-load
 * cache, several threads may load the same missing key at the same time.
 * </p>
 * <p>
 * A time to live of zero or less, or a maximum size of zero or less, disables caching: the loader is called on every
 * {@link #get}.
 * </p>
 *
 * @param <K> key type
 * @param <V> value type, {@code null} values are never cached
 */
public class InvalidationSafeCache<K, V> {

    /**
     * {@code null} when caching is disabled.
     */
    private final Cache<K, V> cache;

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

    public InvalidationSafeCache(Duration ttl, long maxSize) {
        this(ttl, maxSize, Ticker.systemTicker());
    }

    InvalidationSafeCache(Duration ttl, long maxSize, Ticker ticker) {
        boolean disabled = ttl == null || ttl.isZero() || ttl.isNegative() || maxSize <= 0;
        this.cache = disabled ? null : Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize)
                .ticker(ticker)
                .build();
    }

    /**
     * Returns the cached value for the key, or loads it with the given loader when it is missing or expired.
     * The loader may return {@code null}, which is returned to the caller but not cached.
     */
    public V get(K key, Function<K, V> loader) {
        if (cache == null) {
            return loader.apply(key);
        }
        V cached = cache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }
        long generationBeforeLoad = generation;
        V value = loader.apply(key);
        if (value == null) {
            return null;
        }
        synchronized (publishLock) {
            if (generation == generationBeforeLoad) {
                cache.put(key, value);
            }
        }
        return value;
    }

    public void invalidate(K key) {
        if (cache == null) {
            return;
        }
        synchronized (publishLock) {
            generation++;
            cache.invalidate(key);
        }
    }

    public void clear() {
        if (cache == null) {
            return;
        }
        synchronized (publishLock) {
            generation++;
            cache.invalidateAll();
        }
    }

    /**
     * Runs Caffeine's pending maintenance (e.g. size-based eviction) so tests can observe its result.
     */
    void cleanUp() {
        if (cache != null) {
            cache.cleanUp();
        }
    }

    long estimatedSize() {
        return cache == null ? 0 : cache.estimatedSize();
    }
}
