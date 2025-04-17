package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.constants.ActorConstants;
import com.netgrif.application.engine.authorization.domain.params.ActorParams;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActorService implements IActorService {

    private final IDataService dataService;
    private final IIdentityService identityService;
    private final IElasticCaseSearchService elasticCaseSearchService;
    private final IWorkflowService workflowService;

    public ActorService(@Lazy IDataService dataService, IIdentityService identityService,
                        @Lazy IElasticCaseSearchService elasticCaseSearchService, @Lazy IWorkflowService workflowService) {
        this.dataService = dataService;
        this.identityService = identityService;
        this.elasticCaseSearchService = elasticCaseSearchService;
        this.workflowService = workflowService;
    }

    /**
     * todo javadoc
     * */
    @Override
    public Optional<Actor> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return findOneByQuery(emailQuery(email));
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return countByQuery(emailQuery(email)) > 0;
    }

    /**
     * todo javadoc
     * */
    @Override
    public Optional<Actor> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            Case actorCase = workflowService.findOne(id);
            if (!actorCase.getProcessIdentifier().equals("actor")) {
                return Optional.empty();
            }
            return Optional.of(new Actor(actorCase));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        return workflowService.count(QCase.case$.processIdentifier.eq("actor")
                .and(QCase.case$.id.eq(new ObjectId(id)))) > 0;
    }

    @Override
    public List<Actor> findAll() {
        List<Case> result = workflowService.searchAll(QCase.case$.processIdentifier.eq(ActorConstants.PROCESS_IDENTIFIER)).getContent();
        return result.stream().map(Actor::new).collect(Collectors.toList());
    }

    /**
     * todo javadoc
     * */
    @Override
    public Actor create(ActorParams params) {
        throwIfInvalidParams(params);

        String activeActorId = identityService.getActiveActorId();
        Case actorCase = workflowService.createCaseByIdentifier(ActorConstants.PROCESS_IDENTIFIER, params.getFullName(),
                "", activeActorId).getCase();
        actorCase = dataService.setData(actorCase, params.toDataSet(), activeActorId).getCase();
        log.debug("Actor [{}] was created by actor [{}].", actorCase, activeActorId);
        return new Actor(dataService.setData(actorCase, params.toDataSet(), activeActorId).getCase());
    }

    /**
     * todo javadoc
     * */
    @Override
    public Actor update(Actor actor, ActorParams params) {
        throwIfInvalidParams(params);
        if (actor == null) {
            throw new IllegalArgumentException("Please provide actor to be updated");
        }

        String activeActorId = identityService.getActiveActorId();
        return new Actor(dataService.setData(actor.getCase(), params.toDataSet(), activeActorId).getCase());
    }

    private void throwIfInvalidParams(ActorParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for actor");
        }
        if (isTextFieldValueEmpty(params.getEmail())) {
            throw new IllegalArgumentException("Actor must have an email!");
        }
    }

    private boolean isTextFieldValueEmpty(TextField field) {
        return field == null || field.getRawValue() == null || field.getRawValue().trim().isEmpty();
    }

    private Optional<Actor> findOneByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        Page<Case> resultAsPage = elasticCaseSearchService.search(List.of(request), identityService.getLoggedIdentity(), PageRequest.of(0, 1),
                Locale.getDefault(), false, null);
        if (resultAsPage.hasContent()) {
            return Optional.of(new Actor(resultAsPage.getContent().get(0)));
        }
        return Optional.empty();
    }

    private long countByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        return elasticCaseSearchService.count(List.of(request), identityService.getLoggedIdentity(), Locale.getDefault(),
                false, null);
    }

    private static String buildQuery(Set<String> andQueries) {
        StringBuilder queryBuilder = new StringBuilder("processIdentifier:actor");
        for (String query : andQueries) {
            queryBuilder.append(" AND ");
            queryBuilder.append(query);
        }
        return queryBuilder.toString();
    }

    private static String emailQuery(String email) {
        return String.format("dataSet.%s.fulltextValue:\"%s\"", ActorConstants.EMAIL_FIELD_ID, email);
    }
}
