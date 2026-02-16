package com.netgrif.application.engine.configuration.cache;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.workspace.Workspaceable;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleCacheManager;

import java.util.Optional;

// todo javadoc
@Slf4j
@NoArgsConstructor
public class NaeCacheManager extends SimpleCacheManager {

    // todo javadoc
    public <T> Optional<T> getFromCache(String cacheName, Object key, LoggedUser loggedUser) {
        log.trace("Trying to get resource from cache: {}, with key: {}, by logged user: {}...", cacheName, key, loggedUser);
        Cache cache = getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                @SuppressWarnings("unchecked")
                T cachedObject = (T) wrapper.get();
                if (hasWorkspacePermission(cachedObject, loggedUser)) {
                    log.trace("Found resource in cache. Retrieving from the cache...");
                    return Optional.ofNullable(cachedObject);
                } else {
                    log.trace("Could not get cached resource. No workspace permission is present for user: {}", loggedUser.getUsername());
                }
            } else {
                log.trace("Nothing found cached with the key: {}", key);
            }
        } else {
            log.warn("Could not find the cache with name: {}", cacheName);
        }
        return Optional.empty();
    }

    // todo javadoc
    public void putToCache(String cacheName, Object key, Object result) {
        if (cacheName == null || key == null) {
            return;
        }
        log.trace("Trying to put resource in cache: {}, with key: {}...", cacheName, key);
        Cache cache = getCache(cacheName);
        if (cache != null) {
            cache.put(key, result);
            log.trace("Successfully cached the resource with the key: {}", key);
        } else {
            log.warn("Could not find the cache with name: {}", cacheName);
        }
    }

    private boolean hasWorkspacePermission(Object cachedObject, LoggedUser loggedUser) {
        if (cachedObject instanceof Workspaceable resource) {
            return loggedUser == null || loggedUser.isAdmin() || loggedUser.getActiveWorkspaceId().equals(resource.getWorkspaceId());
        }
        return true;
    }
}
