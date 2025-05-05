package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.Actor;

import java.util.Optional;

public interface IAllActorService {
    Optional<Actor> findById(String caseId);
}
