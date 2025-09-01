package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.workflow.service.FieldActionsCacheService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

@Slf4j
public class ActionsCacheWrapper implements Cache {
    private final Cache cache;
    private final FieldActionsCacheService fieldActionsCacheService;


    public ActionsCacheWrapper(Cache cache, FieldActionsCacheService fieldActionsCacheService) {
        this.cache = cache;
        this.fieldActionsCacheService = fieldActionsCacheService;
    }
    @NotNull
    @Override
    public String getName() {
        return cache.getName();
    }
    @NotNull
    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }
    @NotNull
    @Override public ValueWrapper get(@NotNull Object key) {
        return cache.get(key);
    }
    @NotNull
    @Override public <T> T get(@NotNull Object key, @NotNull Class<T> type) {
        return cache.get(key, type);
    }
    @NotNull
    @Override public <T> T get(@NotNull Object key, @NotNull Callable<T> loader) {
        return cache.get(key, loader);
    }
    @Override public void put(@NotNull Object key, @NotNull Object value) {
        cache.put(key, value);
    }
    @Override public void evict(@NotNull Object key) {
        cache.evict(key);
    }

    @Override public void clear() {
        cache.clear();
        log.info("actionsCache cleared from cache actuator");
        fieldActionsCacheService.clearActionCache();
    }
}
