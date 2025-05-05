package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.params.ActorParams;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.startup.DefaultGroupRunner;
import com.netgrif.application.engine.workflow.service.CrudSystemCaseService;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class ActorService<T extends Actor> extends CrudSystemCaseService<T> implements IActorService<T> {

    private final DefaultGroupRunner defaultGroupRunner;

    public ActorService(ISessionManagerService sessionManagerService, IDataService dataService,
                        IWorkflowService workflowService, SystemCaseFactoryRegistry systemCaseFactory,
                        IElasticCaseSearchService elasticCaseSearchService, DefaultGroupRunner defaultGroupRunner) {
        super(sessionManagerService, dataService, workflowService, systemCaseFactory, elasticCaseSearchService);
        this.defaultGroupRunner = defaultGroupRunner;
    }

    @Override
    @Transactional
    public T addGroup(T actor, String groupId) {
        return addGroups(actor, Set.of(groupId));
    }

    @Override
    @Transactional
    public T addGroups(T actor, Set<String> groupIdsToAdd) {
        if (actor == null) {
            throw new IllegalArgumentException("Provided actor is null");
        }
        if (groupIdsToAdd == null || groupIdsToAdd.isEmpty()) {
            throw new IllegalArgumentException("Groups are not provided");
        }

        List<String> groupIds;
        if (actor.getGroupIds() != null) {
            groupIds = new ArrayList<>(actor.getGroupIds());
            groupIds.addAll(groupIdsToAdd);
        } else {
            groupIds = new ArrayList<>(groupIdsToAdd);
        }

        return update(actor, new ActorGroupParams(CaseField.withValue(groupIds)));
    }

    @Override
    @Transactional
    public T removeGroup(T actor, String groupId) {
        return removeGroups(actor, Set.of(groupId));
    }

    @Override
    @Transactional
    public T removeGroups(T actor, Set<String> groupIdsToRemove) {
        if (actor == null) {
            throw new IllegalArgumentException("Provided actor is null");
        }
        if (actor.getGroupIds() == null) {
            return actor;
        }
        if (groupIdsToRemove == null || groupIdsToRemove.isEmpty()) {
            throw new IllegalArgumentException("Groups are not provided");
        }

        List<String> groupIds = new ArrayList<>(actor.getGroupIds());
        groupIds.removeAll(groupIdsToRemove);

        return update(actor, new ActorGroupParams(CaseField.withValue(groupIds)));
    }

    @Override
    public Group getDefaultGroup() {
        return defaultGroupRunner.getDefaultGroup();
    }

    /**
     * todo javadoc
     * */
    protected static class ActorGroupParams extends ActorParams {
        protected ActorGroupParams(CaseField groupIds) {
            super(groupIds);
        }
    }
}
