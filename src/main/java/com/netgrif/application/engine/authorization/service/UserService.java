package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.startup.SystemUserRunner;
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
    private final SystemUserRunner systemUserRunner;

    public UserService(@Lazy IDataService dataService, ISessionManagerService sessionManagerService,
                       @Lazy IElasticCaseSearchService elasticCaseSearchService, @Lazy IWorkflowService workflowService,
                       SystemCaseFactoryRegistry systemCaseFactoryRegistry, IGroupService groupService, SystemUserRunner systemUserRunner) {
        super(sessionManagerService, dataService, workflowService, systemCaseFactoryRegistry, elasticCaseSearchService);
        this.groupService = groupService;
        this.systemUserRunner = systemUserRunner;
    }

    /**
     * Finds user by their email address
     * @param email the email address to search for
     * @return Optional containing User if found, empty Optional if email is null or user not found
     */
    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return findOneByQuery(fulltextFieldQuery(UserConstants.EMAIL_FIELD_ID, email));
    }

    /**
     * Checks whether user with given email exists
     * @param email the email address to check
     * @return true if user exists, false if email is null or user not found
     */
    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return countByQuery(fulltextFieldQuery(UserConstants.EMAIL_FIELD_ID, email)) > 0;
    }

    /**
     * Retrieves the system user account
     *
     * @return User representing the system user
     */
    @Override
    public User getSystemUser() {
        return systemUserRunner.getSystemUser();
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
        UserParams typedParams = (UserParams) params;
        if (isTextFieldOrValueEmpty(typedParams.getEmail())) {
            throw new IllegalArgumentException("User must have an email!");
        }
        if (isForbidden(typedParams.getEmail().getRawValue())) {
            throw new IllegalArgumentException(String.format("User email [%s] is reserved by the system.",
                    typedParams.getEmail().getRawValue()));
        }

        if (isCaseFieldOrValueEmpty(typedParams.getGroupIds())) {
            typedParams.setGroupIds(CaseField.withValue(List.of(groupService.getDefaultGroup().getStringId())));
        }
    }

    @Override
    protected void validateAndFixUpdateParams(CaseParams params) throws IllegalArgumentException {
        UserParams typedParams = (UserParams) params;
        if (typedParams.getEmail() == null) {
            return;
        }
        if (isTextFieldValueEmpty(typedParams.getEmail())) {
            throw new IllegalArgumentException("User must have an email!");
        }
        if (isForbidden(typedParams.getEmail().getRawValue())) {
            throw new IllegalArgumentException(String.format("User email [%s] is reserved by the system.",
                    typedParams.getEmail().getRawValue()));
        }
    }
}
