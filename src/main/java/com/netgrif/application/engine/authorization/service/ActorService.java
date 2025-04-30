package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.service.factory.ActorFactory;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ActorService implements IActorService {

    private final Map<String, ActorFactory<?>> actorFactories;
    private final IWorkflowService workflowService;

    public ActorService(IWorkflowService workflowService) {
        this.workflowService = workflowService;
        this.actorFactories = new ConcurrentHashMap<>();
    }

    /**
     * todo javadoc
     * */
    @Override
    public Optional<Actor> findById(String caseId) {
        if (caseId == null) {
            return Optional.empty();
        }

        Case actorCase;
        try {
            actorCase = workflowService.findOne(caseId);
            ActorFactory<?> factory = this.actorFactories.get(actorCase.getProcessIdentifier());
            if (factory == null) {
                log.warn("Actor case with id [{}] of [{}] hasn't got registered factory", caseId, actorCase.getProcessIdentifier());
                return Optional.empty();
            }
            return Optional.of(factory.createActor(actorCase));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void registerFactory(String actorProcessIdentifier, ActorFactory<?> factory) {
        this.actorFactories.put(actorProcessIdentifier, factory);
        log.debug("Registered actor factory [{}] for process [{}].", factory.getClass(), actorProcessIdentifier);
    }
}
