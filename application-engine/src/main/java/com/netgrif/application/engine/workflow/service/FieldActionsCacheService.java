package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.configuration.properties.RunnerConfigurationProperties;
import com.netgrif.application.engine.elastic.service.executors.MaxSizeHashMap;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FieldActionsCacheService implements IFieldActionsCacheService {

    private final RunnerConfigurationProperties.FieldRunnerProperties properties;

    private IPetriNetService petriNetService;
    private Map<String, Closure> actionsCache;
    private Map<String, List<CachedFunction>> globalFunctionsCache;
    private Map<String, CachedFunction> functionsCache;
    private final GroovyShell shell;

    public FieldActionsCacheService(RunnerConfigurationProperties.FieldRunnerProperties properties, IGroovyShellFactory shellFactory) {
        this.properties = properties;
        this.actionsCache = new MaxSizeHashMap<>(properties.getActionCacheSize());
        this.functionsCache = new MaxSizeHashMap<>(properties.getFunctionsCacheSize());
        this.globalFunctionsCache = new MaxSizeHashMap<>(properties.getGlobalFunctionsCacheSize());
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

        List<CachedFunction> functions = petriNet.getFunctions(FunctionScope.GLOBAL).stream()
                .map(function -> CachedFunction.build(shell, function))
                .collect(Collectors.toList());

        if (!functions.isEmpty()) {
            evaluateCachedFunctions(functions);
            globalFunctionsCache.put(petriNet.getIdentifier(), functions);
        } else {
            globalFunctionsCache.remove(petriNet.getIdentifier());
        }
    }

    @Override
    public void reloadCachedFunctions(PetriNet petriNet) {
        globalFunctionsCache.remove(petriNet.getIdentifier());
        cachePetriNetFunctions(petriNetService.getDefaultVersionByIdentifier(petriNet.getIdentifier()));
    }

    @Override
    public Closure getCompiledAction(Action action, boolean shouldRewriteCachedActions) {
        String stringId = action.getId().toString();
        if (shouldRewriteCachedActions || !actionsCache.containsKey(stringId)) {
            Closure code = (Closure) shell.evaluate("{-> " + action.getDefinition() + "}");
            actionsCache.put(stringId, code);
        }
        return actionsCache.get(stringId);
    }

    @Override
    public List<CachedFunction> getCachedFunctions(List<com.netgrif.application.engine.objects.petrinet.domain.Function> functions) {
        List<CachedFunction> cachedFunctions = new ArrayList<>(functions.size());
        functions.forEach(function -> {
            if (!functionsCache.containsKey(function.getStringId())) {
                functionsCache.put(function.getStringId(), CachedFunction.build(shell, function));
            }
            cachedFunctions.add(functionsCache.get(function.getStringId()));
        });
        return cachedFunctions;
    }

    @Override
    public void cacheAllPetriNetFunctions() {
        Pageable pageable = PageRequest.of(0, 500);
        Page<PetriNet> page = petriNetService.getAllActive(pageable);

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
            page = petriNetService.getAll(pageable);
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
        return new HashMap<>(globalFunctionsCache);
    }

    @Override
    public void clearActionCache() {
        this.actionsCache = new MaxSizeHashMap<>(properties.getActionCacheSize());
    }

    @Override
    public void clearGlobalFunctionCache() {
        this.globalFunctionsCache = new MaxSizeHashMap<>(properties.getGlobalFunctionsCacheSize());
    }

    @Override
    public void clearFunctionCache() {
        this.functionsCache = new MaxSizeHashMap<>(properties.getFunctionsCacheSize());
    }
}
