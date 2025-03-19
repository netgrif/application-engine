package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.constants.ActorConstants;
import com.netgrif.application.engine.authorization.domain.params.ActorParams;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ActorService implements IActorService {

    private final IWorkflowService workflowService;
    private final IDataService dataService;
    private final IIdentityService identityService;
    private final IElasticCaseService elasticCaseService;

    private final IUserService userService;

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
            return Optional.of((Actor) workflowService.findOne(id));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Actor> findAll() {
        List<Case> result = workflowService.searchAll(QCase.case$.processIdentifier.eq(ActorConstants.PROCESS_IDENTIFIER)).getContent();
        return (List<Actor>) (List<? extends Case>) result;
    }

    /**
     * todo javadoc
     * */
    @Override
    public Actor create(ActorParams params) {
        Actor actor = workflowService.createCaseByIdentifier(ActorConstants.PROCESS_IDENTIFIER, params.getFullName(),
                "", identityService.getLoggedIdentity());
        return (Actor) dataService.setData(actor, params.toDataSet(), userService.getSystem()).getCase();
    }

    /**
     * todo javadoc
     * */
    @Override
    public Actor update(Actor actor, ActorParams params) {
        return (Actor) dataService.setData(actor, params.toDataSet(), userService.getSystem()).getCase();
    }

    private Optional<Actor> findOneByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        Page<Case> resultAsPage = elasticCaseService.search(List.of(request), identityService.getLoggedIdentity(), PageRequest.of(0, 1),
                Locale.getDefault(), false);
        if (resultAsPage.hasContent()) {
            return Optional.of((Actor) resultAsPage.getContent().get(0));
        }
        return Optional.empty();
    }

    private long countByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        return elasticCaseService.count(List.of(request), identityService.getLoggedIdentity(), Locale.getDefault(), false);
    }

    private static String buildQuery(Set<String> andQueries) {
        StringBuilder queryBuilder = new StringBuilder("processIdentifier:identity");
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
