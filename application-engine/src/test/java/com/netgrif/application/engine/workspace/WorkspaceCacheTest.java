package com.netgrif.application.engine.workspace;

import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet;
import com.netgrif.application.engine.configuration.cache.NaeCacheManager;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkspaceCacheTest {

    @Autowired
    private NaeCacheManager cacheManager;

    @BeforeEach
    protected void beforeEach() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    private void loginCustomUser(String activeWorkspaceId, boolean isAdmin) {
        LoggedUser loggedUser = new LoggedUserImpl();
        loggedUser.setUsername("username1");
        loggedUser.setActiveWorkspaceId(activeWorkspaceId);
        if (isAdmin) {
            Set<Authority> authorities = new HashSet<>();
            authorities.add(new AuthorityImpl(Authority.admin));
            loggedUser.setAuthoritySet(authorities);
        }
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(loggedUser, "password", null));;
    }

    private void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private LoggedUser getLoggedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : (LoggedUser) auth.getPrincipal();
    }

    @Test
    public void testCache() {
        String workspaceId1 = "workspace1";
        String workspaceId2 = "workspace2";

        PetriNet resource1 = new PetriNet();
        resource1.setWorkspaceId(workspaceId1);

        PetriNet resource2 = new PetriNet();
        resource2.setWorkspaceId(workspaceId2);

        String cacheName = cacheManager.getCacheNames().stream().findAny().get();
        Cache cache = cacheManager.getCache(cacheName);
        assertNotNull(cache);

        cacheManager.putToCache(cacheName, resource1.getStringId(), resource1);
        cacheManager.putToCache(cacheName, resource2.getStringId(), resource2);

        Map<?,?> nativeCache = (Map<?, ?>) cache.getNativeCache();
        assertEquals(2, nativeCache.size());

        logout();
        Optional<Object> cachedOpt = cacheManager.getFromCache(cacheName, resource1.getStringId(), getLoggedUser());
        assertTrue(cachedOpt.isPresent());
        cachedOpt = cacheManager.getFromCache(cacheName, resource2.getStringId(), getLoggedUser());
        assertTrue(cachedOpt.isPresent());

        loginCustomUser("wrongWorkspace", false);
        cachedOpt = cacheManager.getFromCache(cacheName, resource1.getStringId(), getLoggedUser());
        assertFalse(cachedOpt.isPresent());
        cachedOpt = cacheManager.getFromCache(cacheName, resource2.getStringId(), getLoggedUser());
        assertFalse(cachedOpt.isPresent());

        loginCustomUser("wrongWorkspace", true);
        cachedOpt = cacheManager.getFromCache(cacheName, resource1.getStringId(), getLoggedUser());
        assertTrue(cachedOpt.isPresent());
        cachedOpt = cacheManager.getFromCache(cacheName, resource2.getStringId(), getLoggedUser());
        assertTrue(cachedOpt.isPresent());

        loginCustomUser(workspaceId1, false);
        cachedOpt = cacheManager.getFromCache(cacheName, resource1.getStringId(), getLoggedUser());
        assertTrue(cachedOpt.isPresent());
        cachedOpt = cacheManager.getFromCache(cacheName, resource2.getStringId(), getLoggedUser());
        assertFalse(cachedOpt.isPresent());

        loginCustomUser(workspaceId2, false);
        cachedOpt = cacheManager.getFromCache(cacheName, resource1.getStringId(), getLoggedUser());
        assertFalse(cachedOpt.isPresent());
        cachedOpt = cacheManager.getFromCache(cacheName, resource2.getStringId(), getLoggedUser());
        assertTrue(cachedOpt.isPresent());
    }

}
