package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.service.factory.ActorFactory;
import com.netgrif.application.engine.workflow.domain.Case;

import java.util.Optional;

public interface IActorService {
    Optional<Actor> findById(String caseId);
    void registerFactory(String actorProcessIdentifier, ActorFactory<?> factory);
    Actor fromCase(Case actorCase);
}
