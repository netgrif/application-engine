package com.netgrif.application.engine.workflow.service.factory;

import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.service.ActorTypeRegistry;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import org.springframework.stereotype.Service;

@Service
public class GroupFactory extends ActorFactory<Group> {

    public GroupFactory(SystemCaseFactoryRegistry factoryRegistry, ActorTypeRegistry actorTypeRegistry) {
        super(factoryRegistry, actorTypeRegistry);
    }

    @Override
    public Group createObject(Case groupCase) {
        return new Group(groupCase);
    }

    @Override
    protected void registerFactory() {
        factoryRegistry.registerFactory(GroupConstants.PROCESS_IDENTIFIER, this);
    }

    @Override
    protected void registerType() {
        actorTypeRegistry.registerActorType(GroupConstants.PROCESS_IDENTIFIER, Group.class);
    }
}
