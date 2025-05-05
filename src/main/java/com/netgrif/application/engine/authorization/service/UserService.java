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

    public UserService(@Lazy IDataService dataService, ISessionManagerService sessionManagerService,
                       @Lazy IElasticCaseSearchService elasticCaseSearchService, @Lazy IWorkflowService workflowService,
                       SystemCaseFactoryRegistry systemCaseFactoryRegistry) {
        super(sessionManagerService, dataService, workflowService, systemCaseFactoryRegistry, elasticCaseSearchService);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return findOneByQuery(fulltextFieldQuery(UserConstants.EMAIL_FIELD_ID, email));
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return countByQuery(fulltextFieldQuery(UserConstants.EMAIL_FIELD_ID, email)) > 0;
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
}
