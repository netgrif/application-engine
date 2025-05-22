package com.netgrif.application.engine.workflow.service.factory;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.service.ActorTypeRegistry;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;

import javax.annotation.PostConstruct;

public abstract class ActorFactory<T extends Actor> extends SystemCaseFactory<T>{

    protected final ActorTypeRegistry actorTypeRegistry;

    public ActorFactory(SystemCaseFactoryRegistry factoryRegistry, ActorTypeRegistry actorTypeRegistry) {
        super(factoryRegistry);
        this.actorTypeRegistry = actorTypeRegistry;
    }

    // todo javadoc
    @PostConstruct
    protected abstract void registerType();

}
