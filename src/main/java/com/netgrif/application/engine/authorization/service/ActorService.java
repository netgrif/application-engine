package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.params.ActorParams;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.workflow.service.CrudSystemCaseService;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class ActorService<T extends Actor> extends CrudSystemCaseService<T> implements IActorService<T> {

    public ActorService(ISessionManagerService sessionManagerService, IDataService dataService,
                        IWorkflowService workflowService, SystemCaseFactoryRegistry systemCaseFactory,
                        IElasticCaseSearchService elasticCaseSearchService) {
        super(sessionManagerService, dataService, workflowService, systemCaseFactory, elasticCaseSearchService);
    }

    @Override
    @Transactional
    public T addGroup(T actor, String groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("Group is not provided");
        }
        return addGroups(actor, Set.of(groupId));
    }

    @Override
    @Transactional
    public T addGroups(T actor, Set<String> groupIdsToAdd) {
        if (actor == null) {
            throw new IllegalArgumentException("Provided actor is null");
        }
        if (groupIdsToAdd == null) {
            throw new IllegalArgumentException("Groups are not provided");
        }

        groupIdsToAdd = groupIdsToAdd.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (groupIdsToAdd.isEmpty()) {
            return actor;
        }

        Set<String> groupIds;
        if (actor.getGroupIds() != null) {
            groupIds = new HashSet<>(actor.getGroupIds());
            groupIds.addAll(groupIdsToAdd);
        } else {
            groupIds = new HashSet<>(groupIdsToAdd);
        }

        final String activeActorId = sessionManagerService.getActiveActorId();
        return doUpdate(actor, new ActorGroupParams(CaseField.withValue(new ArrayList<>(groupIds))), activeActorId);
    }

    @Override
    @Transactional
    public T removeGroup(T actor, String groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("Group is not provided");
        }
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
        if (groupIdsToRemove == null) {
            throw new IllegalArgumentException("Groups are not provided");
        }
        groupIdsToRemove = groupIdsToRemove.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (groupIdsToRemove.isEmpty()) {
            return actor;
        }

        List<String> groupIds = new ArrayList<>(actor.getGroupIds());
        boolean isAnyRemoved = groupIds.removeAll(groupIdsToRemove);

        if (!isAnyRemoved) {
            return actor;
        }

        final String activeActorId = sessionManagerService.getActiveActorId();
        return doUpdate(actor, new ActorGroupParams(CaseField.withValue(groupIds)), activeActorId);
    }

    /**
     * todo javadoc
     * */
    protected static class ActorGroupParams extends ActorParams {
        protected ActorGroupParams(CaseField groupIds) {
            super(groupIds, null);
        }

        @Override
        public String targetProcessIdentifier() {
            return "";
        }
    }
}
