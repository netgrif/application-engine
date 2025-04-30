package com.netgrif.application.engine.authorization.service.factory;

import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFactory extends ActorFactory<User> {

    private final IActorService actorService;

    @Override
    public User createActor(Case actorCase) {
        return new User(actorCase);
    }

    @Override
    public void registerFactory() {
        actorService.registerFactory(UserConstants.PROCESS_IDENTIFIER, this);
    }
}
