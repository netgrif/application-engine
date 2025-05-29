package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.service.interfaces.IAllActorService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AllActorService implements IAllActorService {

    private final IWorkflowService workflowService;
    private final SystemCaseFactoryRegistry factoryRegistry;
    private final ActorTypeRegistry actorTypeRegistry;

    public AllActorService(IWorkflowService workflowService, SystemCaseFactoryRegistry factoryRegistry,
                           ActorTypeRegistry actorTypeRegistry) {
        this.workflowService = workflowService;
        this.factoryRegistry = factoryRegistry;
        this.actorTypeRegistry = actorTypeRegistry;
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

    @Override
    public List<Actor> findAll() {
        List<Case> cases = workflowService.search(QCase.case$.processIdentifier.in(actorTypeRegistry.getRegisteredProcessIdentifiers()),
                Pageable.unpaged()).getContent();
        return cases.stream().map(useCase -> (Actor) factoryRegistry.fromCase(useCase)).collect(Collectors.toList());
    }
}
