package com.netgrif.application.engine.objects.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvalidationSafeCacheTest {

    @Test
    void loadsOnlyOnceWhileEntryIsFresh() {
        InvalidationSafeCache<String, String> cache = new InvalidationSafeCache<>(Duration.ofMinutes(1), 10);
        AtomicInteger loads = new AtomicInteger();

        assertEquals("v", cache.get("k", key -> {
            loads.incrementAndGet();
            return "v";
        }));
        assertEquals("v", cache.get("k", key -> {
            loads.incrementAndGet();
            return "other";
        }));

        assertEquals(1, loads.get());
    }

    @Test
    void doesNotCacheNullResults() {
        InvalidationSafeCache<String, String> cache = new InvalidationSafeCache<>(Duration.ofMinutes(1), 10);
        AtomicInteger loads = new AtomicInteger();

        assertNull(cache.get("k", key -> {
            loads.incrementAndGet();
            return null;
        }));
        assertEquals("v", cache.get("k", key -> {
            loads.incrementAndGet();
            return "v";
        }));

        assertEquals(2, loads.get());
    }

    @Test
    void reloadsAfterExpiry() {
        AtomicLong nanos = new AtomicLong();
        InvalidationSafeCache<String, String> cache = new InvalidationSafeCache<>(Duration.ofSeconds(30), 10, nanos::get);
        AtomicInteger loads = new AtomicInteger();

        assertEquals("v1", cache.get("k", key -> "v" + loads.incrementAndGet()));
        nanos.addAndGet(Duration.ofSeconds(29).toNanos());
        assertEquals("v1", cache.get("k", key -> "v" + loads.incrementAndGet()));
        nanos.addAndGet(Duration.ofSeconds(2).toNanos());

        assertEquals("v2", cache.get("k", key -> "v" + loads.incrementAndGet()));
    }

    @Test
    void invalidateAndClearForceReload() {
        InvalidationSafeCache<String, String> cache = new InvalidationSafeCache<>(Duration.ofMinutes(1), 10);
        AtomicInteger loads = new AtomicInteger();

        cache.get("a", key -> "v" + loads.incrementAndGet());
        cache.invalidate("a");
        assertEquals("v2", cache.get("a", key -> "v" + loads.incrementAndGet()));

        cache.clear();
        assertEquals("v3", cache.get("a", key -> "v" + loads.incrementAndGet()));
    }

    @Test
    void invalidateOfOneKeyKeepsOtherKeys() {
        InvalidationSafeCache<String, String> cache = new InvalidationSafeCache<>(Duration.ofMinutes(1), 10);
        AtomicInteger loads = new AtomicInteger();
        cache.get("a", key -> "a" + loads.incrementAndGet());
        cache.get("b", key -> "b" + loads.incrementAndGet());

        cache.invalidate("a");

        assertEquals("b2", cache.get("b", key -> "other"));
        assertEquals(2, loads.get());
    }

    @Test
    void doesNotPublishValueLoadedAcrossClear() {
        InvalidationSafeCache<String, String> cache = new InvalidationSafeCache<>(Duration.ofMinutes(1), 10);

        // the clear happens while the loader is still running, i.e. the loaded value may predate the change
        assertEquals("stale", cache.get("k", key -> {
            cache.clear();
            return "stale";
        }));

        assertEquals("fresh", cache.get("k", key -> "fresh"));
    }

    @Test
    void doesNotPublishValueLoadedAcrossInvalidate() {
        InvalidationSafeCache<String, String> cache = new InvalidationSafeCache<>(Duration.ofMinutes(1), 10);

        assertEquals("stale", cache.get("k", key -> {
            cache.invalidate("k");
            return "stale";
        }));

        assertEquals("fresh", cache.get("k", key -> "fresh"));
    }

    @Test
    void loaderMayUseTheSameCache() {
        InvalidationSafeCache<String, String> cache = new InvalidationSafeCache<>(Duration.ofMinutes(1), 10);

        // e.g. loading one entity triggers the lookup of another one through the same cache
        for (int i = 0; i < 200; i++) {
            String inner = "inner" + i;
            assertEquals("outer-inner", cache.get("outer" + i, key -> "outer-" + cache.get(inner, k -> "inner")));
        }
    }

    @Test
    void nonPositiveTtlOrMaxSizeDisablesCaching() {
        AtomicInteger loads = new AtomicInteger();
        List<InvalidationSafeCache<String, String>> disabled = List.of(
                new InvalidationSafeCache<>(Duration.ZERO, 10),
                new InvalidationSafeCache<>(Duration.ofSeconds(-5), 10),
                new InvalidationSafeCache<>(Duration.ofMinutes(1), 0),
                new InvalidationSafeCache<>(null, 10));

        for (InvalidationSafeCache<String, String> cache : disabled) {
            cache.get("k", key -> "v" + loads.incrementAndGet());
            cache.get("k", key -> "v" + loads.incrementAndGet());
            // invalidation of a disabled cache is a no-op
            cache.invalidate("k");
            cache.clear();
            cache.cleanUp();
            assertEquals(0, cache.estimatedSize());
        }

        assertEquals(8, loads.get());
    }

    @Test
    void evictsEntriesBeyondMaxSize() {
        InvalidationSafeCache<String, String> cache = new InvalidationSafeCache<>(Duration.ofMinutes(1), 2);

        for (int i = 0; i < 20; i++) {
            cache.get("k" + i, key -> key.toUpperCase());
        }
        cache.cleanUp();

        assertTrue(cache.estimatedSize() <= 2, "size was " + cache.estimatedSize());
    }

    /**
     * A writer persists a new version and then clears the cache. Once {@code clear()} has returned, no reader that
     * starts afterwards may be served an older version, even when its load was running during the clear.
     */
    @Test
    @Timeout(60)
    void readersNeverSeeOlderVersionThanLastCompletedWrite() throws Exception {
        InvalidationSafeCache<String, Integer> cache = new InvalidationSafeCache<>(Duration.ofMinutes(5), 10);
        AtomicInteger databaseVersion = new AtomicInteger();
        AtomicInteger lastCompletedWrite = new AtomicInteger();
        AtomicReference<String> failure = new AtomicReference<>();
        int writes = 2_000;
        int readers = 4;

        ExecutorService executor = Executors.newFixedThreadPool(readers + 1);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();
            futures.add(executor.submit(() -> {
                start.await();
                for (int version = 1; version <= writes; version++) {
                    databaseVersion.set(version);
                    cache.clear();
                    lastCompletedWrite.set(version);
                }
                return null;
            }));
            for (int r = 0; r < readers; r++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    while (lastCompletedWrite.get() < writes) {
                        int floor = lastCompletedWrite.get();
                        int seen = cache.get("k", key -> {
                            int loaded = databaseVersion.get();
                            Thread.yield();
                            return loaded;
                        });
                        if (seen < floor) {
                            failure.compareAndSet(null, "read version " + seen + " after write " + floor + " completed");
                            return null;
                        }
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(50, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertNull(failure.get());
    }

    /**
     * Documents the Caffeine behaviour this class exists for. If it changes in a newer Caffeine version, this class
     * may become unnecessary.
     */
    @Test
    @Timeout(30)
    void plainCaffeineKeepsValueOfLoadRunningDuringInvalidateAll() throws Exception {
        Cache<String, String> caffeine = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).maximumSize(10).build();
        CountDownLatch loading = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Thread loader = new Thread(() -> caffeine.get("k", key -> {
            loading.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return "stale";
        }));
        loader.start();
        loading.await();
        caffeine.invalidateAll();
        release.countDown();
        loader.join();

        assertEquals("stale", caffeine.getIfPresent("k"));
    }

    @Test
    @Timeout(30)
    void plainCaffeineRejectsLoaderUsingTheSameCache() {
        Cache<String, String> caffeine = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).maximumSize(1000).build();

        assertThrows(IllegalStateException.class, () -> {
            for (int i = 0; i < 200; i++) {
                String inner = "inner" + i;
                caffeine.get("outer" + i, key -> "outer-" + caffeine.get(inner, k -> "inner"));
            }
        });
    }
}
