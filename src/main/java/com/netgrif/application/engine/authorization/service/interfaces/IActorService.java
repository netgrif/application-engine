package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.params.ActorParams;

import java.util.List;
import java.util.Optional;

public interface IActorService {
    Optional<Actor> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Actor> findById(String id);
    List<Actor> findAll();

    Actor create(ActorParams params);
    Actor update(Actor actor, ActorParams params);
}
