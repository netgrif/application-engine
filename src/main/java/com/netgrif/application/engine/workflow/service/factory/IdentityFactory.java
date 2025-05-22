package com.netgrif.application.engine.workflow.service.factory;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.constants.IdentityConstants;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import org.springframework.stereotype.Service;

@Service
public class IdentityFactory extends SystemCaseFactory<Identity> {

    public IdentityFactory(SystemCaseFactoryRegistry factoryRegistry) {
        super(factoryRegistry);
    }

    @Override
    public Identity createObject(Case identityCase) {
        return new Identity(identityCase);
    }

    @Override
    protected void registerFactory() {
        factoryRegistry.registerFactory(IdentityConstants.PROCESS_IDENTIFIER, this);
    }
}
