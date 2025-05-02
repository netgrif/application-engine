package com.netgrif.application.engine.workflow.service.factory;

import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFactory extends SystemCaseFactory<User> {

    private final SystemCaseFactoryRegistry factoryRegistry;

    @Override
    public User createObject(Case userCase) {
        return new User(userCase);
    }

    @Override
    public void registerFactory() {
        factoryRegistry.registerFactory(UserConstants.PROCESS_IDENTIFIER, this);
    }
}
