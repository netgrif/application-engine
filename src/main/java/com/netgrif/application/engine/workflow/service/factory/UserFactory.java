package com.netgrif.application.engine.workflow.service.factory;

import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.service.ActorTypeRegistry;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import org.springframework.stereotype.Service;

@Service
public class UserFactory extends ActorFactory<User> {

    public UserFactory(SystemCaseFactoryRegistry factoryRegistry, ActorTypeRegistry actorTypeRegistry) {
        super(factoryRegistry, actorTypeRegistry);
    }

    @Override
    public User createObject(Case userCase) {
        return new User(userCase);
    }

    @Override
    protected void registerFactory() {
        factoryRegistry.registerFactory(UserConstants.PROCESS_IDENTIFIER, this);
    }

    @Override
    protected void registerType() {
        actorTypeRegistry.registerActorType(UserConstants.PROCESS_IDENTIFIER, User.class);
    }
}
