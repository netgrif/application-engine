package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import org.springframework.stereotype.Service;

@Service
public class ActorService implements IActorService {
    @Override
    public Actor findByEmail(String email) {
        return null;
    }
}
