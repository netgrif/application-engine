package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.SystemCase;
import com.netgrif.application.engine.workflow.service.factory.SystemCaseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SystemCaseFactoryRegistry {

    /**
     * Key is a process identifier and value is a factory
     * */
    private final Map<String, SystemCaseFactory<?>> factories;

    public SystemCaseFactoryRegistry() {
        this.factories = new ConcurrentHashMap<>();
    }

    /**
     * todo javadoc
     * */
    public void registerFactory(String processIdentifier, SystemCaseFactory<?> factory) {
        this.factories.put(processIdentifier, factory);
        log.debug("Registered system case factory [{}] for process [{}].", factory.getClass(), processIdentifier);
    }

    /**
     * todo javadoc
     * */
    public SystemCase fromCase(Case systemCase) {
        if (systemCase == null) {
            return null;
        }
        SystemCaseFactory<?> factory = this.factories.get(systemCase.getProcessIdentifier());
        if (factory == null) {
            log.warn("System case with id [{}] of [{}] hasn't got registered factory", systemCase.getStringId(),
                    systemCase.getProcessIdentifier());
            return null;
        }
        return factory.createObject(systemCase);
    }
}
