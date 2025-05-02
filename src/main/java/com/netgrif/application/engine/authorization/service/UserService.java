package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.SystemCase;
import com.netgrif.application.engine.workflow.service.CrudSystemCaseService;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService extends CrudSystemCaseService<User> implements IUserService {

    private final IElasticCaseSearchService elasticCaseSearchService;

    public UserService(@Lazy IDataService dataService, ISessionManagerService sessionManagerService,
                       @Lazy IElasticCaseSearchService elasticCaseSearchService, @Lazy IWorkflowService workflowService,
                       SystemCaseFactoryRegistry systemCaseFactoryRegistry) {
        super(sessionManagerService, dataService, workflowService, systemCaseFactoryRegistry);
        this.elasticCaseSearchService = elasticCaseSearchService;
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

    @Override
    public List<User> findAll() {
        List<Case> result = workflowService.searchAll(QCase.case$.processIdentifier.eq(UserConstants.PROCESS_IDENTIFIER)).getContent();
        return result.stream().map(User::new).collect(Collectors.toList());
    }

    @Override
    protected String getProcessIdentifier() {
        return UserConstants.PROCESS_IDENTIFIER;
    }

    @Override
    protected void validateCreateParams(CaseParams params) throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for user");
        }
        UserParams typedParams = (UserParams) params;
        if (isTextFieldOrValueEmpty(typedParams.getEmail())) {
            throw new IllegalArgumentException("User must have an email!");
        }
    }

    @Override
    protected void validateUpdateParams(CaseParams params) throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for user");
        }
        UserParams typedParams = (UserParams) params;
        if (typedParams.getEmail() != null && isTextFieldValueEmpty(typedParams.getEmail())) {
            throw new IllegalArgumentException("User must have an email!");
        }
    }

    @Override
    protected void postUpdateActions(SystemCase systemCase) {
        // none
    }

    private Optional<User> findOneByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        Page<Case> resultAsPage = elasticCaseSearchService.search(List.of(request), sessionManagerService.getLoggedIdentity(),
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
        return elasticCaseSearchService.count(List.of(request), sessionManagerService.getLoggedIdentity(), Locale.getDefault(),
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
