package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Actor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ActorTypeRegistry {

    /**
     * Key is a process identifier and value is a factory
     * */
    private final Map<String, Class<?>> types;

    public ActorTypeRegistry() {
        this.types = new ConcurrentHashMap<>();
    }

    public Set<String> getRegisteredProcessIdentifiers() {
        return this.types.keySet();
    }


    // todo javadoc
    public void registerActorType(String processIdentifier, Class<?> implementationClass) {
        if (!implementationClass.getSuperclass().equals(Actor.class)) {
            log.warn("Cannot register [{}] as actor type, since it's not actor type.", processIdentifier);
            return;
        }
        this.types.put(processIdentifier, implementationClass);
        log.info("Registered [{}] as actor type", processIdentifier);
    }
}
