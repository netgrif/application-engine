package com.netgrif.application.engine.workflow.service.factory;

import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupFactory extends SystemCaseFactory<Group> {

    private final SystemCaseFactoryRegistry factoryRegistry;

    @Override
    public Group createObject(Case groupCase) {
        return new Group(groupCase);
    }

    @Override
    protected void registerFactory() {
        factoryRegistry.registerFactory(GroupConstants.PROCESS_IDENTIFIER, this);
    }
}
