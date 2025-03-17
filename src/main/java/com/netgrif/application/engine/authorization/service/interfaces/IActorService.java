package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.Actor;

public interface IActorService {
    Actor findByEmail(String email);
}
