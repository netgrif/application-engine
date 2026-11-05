package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.configuration.CacheMapKeys;
import com.netgrif.application.engine.configuration.properties.CacheConfigurationProperties;
import com.netgrif.application.engine.event.IGroovyShellFactory;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.Function;
import com.netgrif.application.engine.objects.petrinet.domain.FunctionScope;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.domain.CachedFunction;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FieldActionsCacheService implements IFieldActionsCacheService {

    private final CacheConfigurationProperties properties;
    private final CacheManager cacheManager;

    private IPetriNetService petriNetService;

    private final GroovyShell shell;

    public FieldActionsCacheService(CacheConfigurationProperties properties, CacheManager cacheManager, IGroovyShellFactory shellFactory) {
        this.properties = properties;
        this.cacheManager = cacheManager;
        this.shell = shellFactory.getGroovyShell();
    }

    @Autowired
    @Lazy
    public void setPetriNetService(IPetriNetService petriNetService) {
        this.petriNetService = petriNetService;
    }

    @Override
    public void cachePetriNetFunctions(PetriNet petriNet) {
        if (petriNet == null) {
            return;
        }

        log.info("Caching functions for PetriNet id={}", petriNet.getIdentifier());

        List<CachedFunction> functions = petriNet.getFunctions(FunctionScope.GLOBAL).stream()
                .map(function -> CachedFunction.build(shell, function))
                .collect(Collectors.toList());

        Cache globalFunctionsCache = getRequiredCache(CacheMapKeys.GLOBAL_FUNCTIONS);

        if (!functions.isEmpty()) {
            evaluateCachedFunctions(functions);
            globalFunctionsCache.put(petriNet.getIdentifier(), functions);
        } else {
            globalFunctionsCache.evictIfPresent(petriNet.getIdentifier());
        }
        log.info("Finished caching functions for PetriNet id={}", petriNet.getIdentifier());
    }

    @Override
    public void removeCachedPetriNetFunctions(String processIdentifier) {
        getRequiredCache(CacheMapKeys.GLOBAL_FUNCTIONS).evictIfPresent(processIdentifier);
    }

    @Override
    public void reloadCachedGlobalFunctions(String processIdentifier) {
        removeCachedPetriNetFunctions(processIdentifier);
        PetriNet petriNet = petriNetService.getDefaultVersionByIdentifier(processIdentifier);
        if (petriNet != null) {
            cachePetriNetFunctions(petriNet);
        }
    }

    @Override
    public void reloadCachedGlobalFunctions(PetriNet petriNet) {
        if (petriNet != null) {
            this.reloadCachedGlobalFunctions(petriNet.getIdentifier());
        }
    }

    @Override
    public Closure getCompiledAction(Action action, boolean shouldRewriteCachedActions) {
        String stringId = action.getId().toString();
        Cache actionsCache = getRequiredCache(CacheMapKeys.ACTIONS);

        Cache.ValueWrapper wrapper = actionsCache.get(stringId);
        if (shouldRewriteCachedActions || wrapper == null) {
            boolean traceEnabled = log.isTraceEnabled();
            String fingerprint = traceEnabled ? actionFingerprint(action) : null;
            long startedAt = traceEnabled ? System.nanoTime() : 0;
            if (traceEnabled) {
                String reason = shouldRewriteCachedActions ? "rewrite-requested" : "cache-miss";
                log.trace("Compiling Groovy action: reason={}, actionId={}, importId={}, trigger={}, definitionFingerprint={}",
                        reason, stringId, action.getImportId(), action.getTrigger(), fingerprint);
            }
            try {
                Closure code = (Closure) shell.evaluate("{-> " + action.getDefinition() + "}");
                actionsCache.put(stringId, code);
                if (traceEnabled) {
                    log.trace("Compiled Groovy action: actionId={}, importId={}, trigger={}, definitionFingerprint={}, durationMs={}",
                            stringId, action.getImportId(), action.getTrigger(), fingerprint, elapsedMillis(startedAt));
                }
                return code;
            } catch (RuntimeException e) {
                if (traceEnabled) {
                    log.trace("Groovy action compilation failed: actionId={}, importId={}, trigger={}, definitionFingerprint={}, durationMs={}",
                            stringId, action.getImportId(), action.getTrigger(), fingerprint, elapsedMillis(startedAt), e);
                }
                throw e;
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Groovy action cache hit: actionId={}, importId={}, trigger={}",
                    stringId, action.getImportId(), action.getTrigger());
        }
        return (Closure) wrapper.get();
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String actionFingerprint(Action action) {
        String definition = action.getDefinition() == null ? "" : action.getDefinition();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(definition.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is not available", e);
        }
    }

    @Override
    public List<CachedFunction> getCachedFunctions(List<Function> functions) {
        List<CachedFunction> cachedFunctions = new ArrayList<>(functions.size());
        Cache functionsCache = getRequiredCache(CacheMapKeys.FUNCTIONS);
        
        for (Function function : functions) {
            Cache.ValueWrapper wrapper = functionsCache.get(function.getStringId());
            CachedFunction cached;
            if (wrapper == null) {
                cached = CachedFunction.build(shell, function);
                functionsCache.put(function.getStringId(), cached);
            } else {
                cached = (CachedFunction) wrapper.get();
            }
            cachedFunctions.add(cached);
        }
        return cachedFunctions;
    }

    @Override
    public void cacheAllPetriNetFunctions() {
        Pageable pageable = PageRequest.of(0, properties.getFunctionCachingPageSize());
        Page<PetriNet> page = petriNetService.getAllDefault(pageable);

        while (!page.isEmpty()) {
            for (PetriNet petriNet : page) {
                try {
                    cachePetriNetFunctions(petriNet);
                } catch (Exception e) {
                    log.warn("Failed to cache functions for PetriNet id={}", petriNet.getStringId(), e);
                }
            }

            if (!page.hasNext()) {
                break;
            }
            pageable = pageable.next();
            page = petriNetService.getAllDefault(pageable);
        }
    }

    @Override
    public void evaluateFunctions(List<Function> functions) {
        evaluateCachedFunctions(functions.stream().map(function -> CachedFunction.build(shell, function)).collect(Collectors.toList()));
    }

    private void evaluateCachedFunctions(List<CachedFunction> cachedFunctions) {
        cachedFunctions.stream()
                .collect(Collectors.groupingBy(this::createKey))
                .forEach((key, value) -> {
                    if (value.size() > 1) {
                        throw new IllegalArgumentException("Duplicate method signature " + key);
                    }
                });
    }

    private String createKey(CachedFunction cachedFunction) {
        return cachedFunction.getFunction().getName() + stringifyParameterTypes(cachedFunction.getCode().getParameterTypes());
    }

    private String stringifyParameterTypes(Class[] a) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "()";

        StringBuilder b = new StringBuilder();
        b.append('(');
        for (int i = 0; ; i++) {
            b.append(a[i].getName());
            if (i == iMax)
                return b.append(')').toString();
            b.append(", ");
        }
    }

    @Override
    public Map<String, List<CachedFunction>> getGlobalFunctionsCache() {
        Object nativeCache = getRequiredCache(CacheMapKeys.GLOBAL_FUNCTIONS).getNativeCache();
        if (nativeCache instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, List<CachedFunction>> typedMap = (Map<String, List<CachedFunction>>) map;
            return new HashMap<>(typedMap);
        }
        return Collections.emptyMap();
    }

    @Override
    public void clearActionCache() {
        Cache actionsCache = getRequiredCache(CacheMapKeys.ACTIONS);
        if (log.isTraceEnabled()) {
            Object nativeCache = actionsCache.getNativeCache();
            int entries = nativeCache instanceof Map<?, ?> map ? map.size() : -1;
            log.trace("Clearing Groovy action cache: entries={}", entries);
        }
        actionsCache.clear();
    }

    @Override
    public void clearGlobalFunctionCache() {
        getRequiredCache(CacheMapKeys.GLOBAL_FUNCTIONS).clear();
    }

    @Override
    public void clearFunctionCache() {
        getRequiredCache(CacheMapKeys.FUNCTIONS).clear();
    }

    private Cache getRequiredCache(String name) {
        Cache cache = cacheManager.getCache(name);
        if (cache == null) {
            throw new IllegalStateException("Cache '" + name + "' is not configured");
        }
        return cache;
    }
}
