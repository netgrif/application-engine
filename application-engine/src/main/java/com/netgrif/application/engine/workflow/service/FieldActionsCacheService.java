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
import org.springframework.stereotype.Service;

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

        List<CachedFunction> functions = petriNet.getFunctions(FunctionScope.NAMESPACE).stream()
                .map(function -> CachedFunction.build(shell, function))
                .collect(Collectors.toList());

        Cache namespaceFunctionsCache = getRequiredCache(properties.getNamespaceFunctions());

        if (!functions.isEmpty()) {
            evaluateCachedFunctions(functions);
            namespaceFunctionsCache.put(petriNet.getIdentifier(), functions);
        } else {
            namespaceFunctionsCache.evictIfPresent(petriNet.getIdentifier());
        }
    }

    @Override
    public void reloadCachedFunctions(String petriNetId) {
        getRequiredCache(properties.getNamespaceFunctions()).evictIfPresent(petriNetId);
        cachePetriNetFunctions(petriNetService.getNewestVersionByIdentifier(petriNetId));
    }

    @Override
    public void reloadCachedFunctions(PetriNet petriNet) {
        this.reloadCachedFunctions(petriNet.getIdentifier());
    }

    @Override
    public Closure getCompiledAction(Action action, boolean shouldRewriteCachedActions) {
        String stringId = action.getId().toString();
        Cache actionsCache = getRequiredCache(CacheMapKeys.ACTIONS);
        Object nativeActionsCache = actionsCache.getNativeCache();

        if (nativeActionsCache instanceof Map<?, ?> map) {
            if (shouldRewriteCachedActions || !map.containsKey(stringId) ) {
                Closure code = (Closure) shell.evaluate("{-> " + action.getDefinition() + "}");
                actionsCache.put(stringId, code);
            }
        }
        return (Closure) actionsCache.get(stringId).get();
    }

    @Override
    public List<CachedFunction> getCachedFunctions(List<Function> functions) {
        List<CachedFunction> cachedFunctions = new ArrayList<>(functions.size());
        Cache functionsCache = getRequiredCache(CacheMapKeys.FUNCTIONS);
        Object nativeFunctionsCache = functionsCache.getNativeCache();

        if (nativeFunctionsCache instanceof Map<?, ?> map) {
            functions.forEach(function -> {
                if (!map.containsKey(function.getStringId())) {
                    functionsCache.put(function.getStringId(), CachedFunction.build(shell, function));
                }
                cachedFunctions.add((CachedFunction) functionsCache.get(function.getStringId()).get());
            });
        }
        return cachedFunctions;
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
    public Map<String, List<CachedFunction>> getNamespaceFunctionCache() {
        return new HashMap<>((Map) getRequiredCache(properties.getNamespaceFunctions()).getNativeCache());
    }

    @Override
    public void clearActionCache() {
        getRequiredCache(CacheMapKeys.ACTIONS).clear();
    }

    @Override
    public void clearNamespaceFunctionCache() {
        getRequiredCache(properties.getNamespaceFunctions()).clear();
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
