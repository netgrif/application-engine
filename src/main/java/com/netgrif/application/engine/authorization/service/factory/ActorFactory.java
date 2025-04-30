package com.netgrif.application.engine.authorization.service.factory;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.workflow.domain.Case;

import javax.annotation.PostConstruct;

public abstract class ActorFactory<T extends Actor> {
    /**
     * todo javadoc
     * */
    public abstract T createActor(Case actorCase);

    /**
     * todo javadoc
     * */
    @PostConstruct
    protected abstract void registerFactory();
}
