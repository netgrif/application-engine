package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
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
public class UserService implements IUserService {

    private final IDataService dataService;
    private final IIdentityService identityService;
    private final IElasticCaseSearchService elasticCaseSearchService;
    private final IWorkflowService workflowService;

    public UserService(@Lazy IDataService dataService, IIdentityService identityService,
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
    public Optional<User> findByEmail(String email) {
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
    public Optional<User> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            Case userCase = workflowService.findOne(id);
            if (!userCase.getProcessIdentifier().equals(UserConstants.PROCESS_IDENTIFIER)) {
                return Optional.empty();
            }
            return Optional.of(new User(userCase));
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
        return workflowService.count(QCase.case$.processIdentifier.eq(UserConstants.PROCESS_IDENTIFIER)
                .and(QCase.case$.id.eq(new ObjectId(id)))) > 0;
    }

    @Override
    public List<User> findAll() {
        List<Case> result = workflowService.searchAll(QCase.case$.processIdentifier.eq(UserConstants.PROCESS_IDENTIFIER)).getContent();
        return result.stream().map(User::new).collect(Collectors.toList());
    }

    /**
     * todo javadoc
     * */
    @Override
    public User create(UserParams params) {
        throwIfInvalidParams(params);

        String activeActorId = identityService.getActiveActorId();
        Case userCase = workflowService.createCaseByIdentifier(UserConstants.PROCESS_IDENTIFIER, params.getFullName(),
                "", activeActorId).getCase();
        userCase = dataService.setData(userCase, params.toDataSet(), activeActorId).getCase();
        log.debug("User [{}] was created by actor [{}].", userCase, activeActorId);
        return new User(dataService.setData(userCase, params.toDataSet(), activeActorId).getCase());
    }

    /**
     * todo javadoc
     * */
    @Override
    public User update(User user, UserParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for user");
        }
        if (params.getEmail() != null && isTextFieldValueEmpty(params.getEmail())) {
            throw new IllegalArgumentException("User must have an email!");
        }
        if (user == null) {
            throw new IllegalArgumentException("Please provide user to be updated");
        }

        String activeActorId = identityService.getActiveActorId();
        return new User(dataService.setData(user.getCase(), params.toDataSet(), activeActorId).getCase());
    }

    private void throwIfInvalidParams(UserParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for user");
        }
        if (isTextFieldOrValueEmpty(params.getEmail())) {
            throw new IllegalArgumentException("User must have an email!");
        }
    }

    private boolean isTextFieldOrValueEmpty(TextField field) {
        return field == null || isTextFieldValueEmpty(field);
    }

    private boolean isTextFieldValueEmpty(TextField field) {
        return field.getRawValue() == null || field.getRawValue().trim().isEmpty();
    }

    private Optional<User> findOneByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        Page<Case> resultAsPage = elasticCaseSearchService.search(List.of(request), identityService.getLoggedIdentity(),
                PageRequest.of(0, 1), Locale.getDefault(), false, null);
        if (resultAsPage.hasContent()) {
            return Optional.of(new User(resultAsPage.getContent().get(0)));
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
        StringBuilder queryBuilder = new StringBuilder("processIdentifier:").append(UserConstants.PROCESS_IDENTIFIER);
        for (String query : andQueries) {
            queryBuilder.append(" AND ");
            queryBuilder.append(query);
        }
        return queryBuilder.toString();
    }

    private static String emailQuery(String email) {
        return String.format("dataSet.%s.fulltextValue:\"%s\"", UserConstants.EMAIL_FIELD_ID, email);
    }
}
