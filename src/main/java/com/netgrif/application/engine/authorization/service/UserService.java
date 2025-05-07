package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserService extends ActorService<User> implements IUserService {

    private final IGroupService groupService;

    public UserService(@Lazy IDataService dataService, ISessionManagerService sessionManagerService,
                       @Lazy IElasticCaseSearchService elasticCaseSearchService, @Lazy IWorkflowService workflowService,
                       SystemCaseFactoryRegistry systemCaseFactoryRegistry, IGroupService groupService) {
        super(sessionManagerService, dataService, workflowService, systemCaseFactoryRegistry, elasticCaseSearchService);
        this.groupService = groupService;
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
    protected String isUniqueQuery(CaseParams params) {
        UserParams typedParams = (UserParams) params;
        return fulltextFieldQuery(UserConstants.EMAIL_FIELD_ID, typedParams.getEmail().getRawValue());
    }

    @Override
    protected void validateAndFixCreateParams(CaseParams params) throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for user");
        }
        UserParams typedParams = (UserParams) params;
        if (isTextFieldOrValueEmpty(typedParams.getEmail())) {
            throw new IllegalArgumentException("User must have an email!");
        }
        if (isForbidden(typedParams.getEmail().getRawValue())) {
            throw new IllegalArgumentException(String.format("User email [%s] is reserved by system.",
                    typedParams.getEmail().getRawValue()));
        }

        if (isCaseFieldOrValueEmpty(typedParams.getGroupIds())) {
            typedParams.setGroupIds(CaseField.withValue(List.of(groupService.getDefaultGroup().getStringId())));
        }
    }

    @Override
    protected void validateAndFixUpdateParams(CaseParams params) throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for user");
        }
        UserParams typedParams = (UserParams) params;
        if (typedParams.getEmail() == null) {
            return;
        }
        if (isTextFieldValueEmpty(typedParams.getEmail())) {
            throw new IllegalArgumentException("User must have an email!");
        }
        if (isForbidden(typedParams.getEmail().getRawValue())) {
            throw new IllegalArgumentException(String.format("User email [%s] is reserved by system.",
                    typedParams.getEmail().getRawValue()));
        }
    }
}
