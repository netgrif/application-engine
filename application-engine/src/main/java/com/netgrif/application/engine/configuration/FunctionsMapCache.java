package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.event.IGroovyShellFactory;
import com.netgrif.application.engine.objects.petrinet.domain.Function;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.domain.CachedFunction;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.Map;

@Slf4j
public class FunctionsMapCache extends GenericMapCache {

    private final GroovyShell shell;

    public FunctionsMapCache(String name, java.util.function.Supplier<Map<String, CachedFunction>> mapFactory, IFieldActionsCacheService fieldActionsCacheService, IPetriNetService petriNetService, IGroovyShellFactory shellFactory) {
        super(name, CachedFunction.class, mapFactory, fieldActionsCacheService, petriNetService);
        this.shell = shellFactory.getGroovyShell();
    }

    @Override
    public synchronized ValueWrapper get(Object key) {
        String stringKey = String.valueOf(key);

        Object valueObject = map().get(stringKey);
        if (valueObject != null) {
            return new SimpleValueWrapper(CachedFunction.copyOf(shell, (CachedFunction) valueObject));
        }

        Function function = petriNetService.findByFunctionId(stringKey);
        if (function != null) {
            map().put(stringKey, CachedFunction.build(shell, function));
            return new SimpleValueWrapper(CachedFunction.build(shell, function));
        }

        return new SimpleValueWrapper(null);
    }

    public synchronized  <T> T get(Object key, Class<T> type) {
        String stringKey = String.valueOf(key);
        Object valueObject = map().get(stringKey);

        if (valueObject != null) {
            return type.cast(valueObject);
        }

        Function function = petriNetService.findByFunctionId(stringKey);
        if (function != null) {
            map().put(stringKey, CachedFunction.build(shell, function));
            return type.cast(CachedFunction.build(shell, function));
        }

        return type.cast(null);
    }
}
