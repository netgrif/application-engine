package com.netgrif.application.engine.configuration.cache;

import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNull;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class NaeCacheManagerTest {

    @Autowired
    private NaeCacheManager cacheManager;

    @BeforeEach
    protected void beforeEach() {
        for(String cacheName : cacheManager.getCacheNames()) {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        }
    }

    @Test
    public void testCachePut() {
        cacheManager.putToCache("notExistingCache", "someKey", new Object());

        String existingCacheName = cacheManager.getCacheNames().stream().findFirst().orElseThrow();

        cacheManager.putToCache(existingCacheName, null, new Object());
        assertCacheSize(existingCacheName, 0);

        cacheManager.putToCache(existingCacheName, "myKey", null);
        assertCacheSize(existingCacheName, 1);
        assertNull(cacheManager.getCache(existingCacheName).get("myKey").get());

        cacheManager.putToCache(existingCacheName, "myKey", new Object());
        assertCacheSize(existingCacheName, 1);
        assertNotNull(cacheManager.getCache(existingCacheName).get("myKey").get());
    }

    @Test
    public void testCacheGet() {
        assertTrue(() -> cacheManager.getFromCache("notExistingCache", "someKey", null).isEmpty());

        String existingCacheName = cacheManager.getCacheNames().stream().findFirst().orElseThrow();

        assertFalse(() -> cacheManager.getFromCache(existingCacheName, "someKey", null).isPresent());

        String cacheKey = "resource1";
        PetriNet resource = new PetriNet();
        resource.setWorkspaceId("workspace1");

        cacheManager.putToCache(existingCacheName, cacheKey, resource);
        assertCacheSize(existingCacheName, 1);

        assertTrue(() -> cacheManager.getFromCache(existingCacheName, cacheKey, null).isPresent());

        LoggedUser loggedUser = new LoggedUserImpl();
        loggedUser.setActiveWorkspaceId("differentWorkspace");

        assertFalse(() -> cacheManager.getFromCache(existingCacheName, cacheKey, loggedUser).isPresent());

        loggedUser.setActiveWorkspaceId("workspace1");
        assertTrue(() -> cacheManager.getFromCache(existingCacheName, cacheKey, loggedUser).isPresent());
    }

    private void assertCacheSize(String cacheName, int expectedSize) {
        Map<?,?> nativeCache = (Map<?, ?>) Objects.requireNonNull(cacheManager.getCache(cacheName)).getNativeCache();
        assertEquals(expectedSize, nativeCache.size());
    }
}
