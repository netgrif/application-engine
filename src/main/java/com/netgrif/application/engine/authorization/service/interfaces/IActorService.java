package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.params.ActorParams;

import java.util.List;
import java.util.Optional;

public interface IActorService {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findById(String id);
    boolean existsById(String id);
    List<User> findAll();

    User create(ActorParams params);
    User update(User user, ActorParams params);
}
