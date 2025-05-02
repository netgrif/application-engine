package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ActorService implements IActorService {

    private final IWorkflowService workflowService;
    private final SystemCaseFactoryRegistry factoryRegistry;

    public ActorService(IWorkflowService workflowService, SystemCaseFactoryRegistry factoryRegistry) {
        this.workflowService = workflowService;
        this.factoryRegistry = factoryRegistry;
    }

    /**
     * todo javadoc
     * */
    @Override
    public Optional<Actor> findById(String caseId) {
        // todo: release/8.0.0 for example for identity case, it returns optional, that is present. Is it an issue?
        if (caseId == null) {
            return Optional.empty();
        }
        try {
            Actor actor = (Actor) factoryRegistry.fromCase(workflowService.findOne(caseId));
            return Optional.ofNullable(actor);
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
