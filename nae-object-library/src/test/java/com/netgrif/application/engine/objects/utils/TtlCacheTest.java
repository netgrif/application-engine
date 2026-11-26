package com.netgrif.application.engine.objects.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TtlCacheTest {

    @Test
    void loadsOnlyOnceWhileEntryIsFresh() {
        TtlCache<String, String> cache = new TtlCache<>(Duration.ofMinutes(1), 10);
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
        TtlCache<String, String> cache = new TtlCache<>(Duration.ofMinutes(1), 10);
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
    void reloadsAfterExpiry() throws InterruptedException {
        TtlCache<String, String> cache = new TtlCache<>(Duration.ofMillis(20), 10);
        AtomicInteger loads = new AtomicInteger();

        cache.get("k", key -> "v" + loads.incrementAndGet());
        Thread.sleep(60);

        assertEquals("v2", cache.get("k", key -> "v" + loads.incrementAndGet()));
    }

    @Test
    void invalidateAndClearForceReload() {
        TtlCache<String, String> cache = new TtlCache<>(Duration.ofMinutes(1), 10);
        AtomicInteger loads = new AtomicInteger();

        cache.get("a", key -> "v" + loads.incrementAndGet());
        cache.invalidate("a");
        assertEquals("v2", cache.get("a", key -> "v" + loads.incrementAndGet()));

        cache.clear();
        assertEquals("v3", cache.get("a", key -> "v" + loads.incrementAndGet()));
    }

    @Test
    void clearsWholeCacheWhenMaxSizeIsReached() {
        TtlCache<String, String> cache = new TtlCache<>(Duration.ofMinutes(1), 2);
        AtomicInteger loads = new AtomicInteger();

        cache.get("a", key -> "v" + loads.incrementAndGet());
        cache.get("b", key -> "v" + loads.incrementAndGet());
        cache.get("c", key -> "v" + loads.incrementAndGet());

        // "a" was evicted when "c" was added, so it is loaded again
        assertEquals("v4", cache.get("a", key -> "v" + loads.incrementAndGet()));
    }
}
