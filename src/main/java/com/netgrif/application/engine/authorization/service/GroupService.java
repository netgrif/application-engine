package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.domain.SystemCase;
import com.netgrif.application.engine.workflow.service.CrudSystemCaseService;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GroupService extends CrudSystemCaseService<Group> implements IGroupService {

    public GroupService(ISessionManagerService sessionManagerService, IDataService dataService,
                        IWorkflowService workflowService, SystemCaseFactoryRegistry systemCaseFactory,
                        IElasticCaseSearchService elasticCaseSearchService) {
        super(sessionManagerService, dataService, workflowService, systemCaseFactory, elasticCaseSearchService);
    }

    @Override
    protected String getProcessIdentifier() {
        return GroupConstants.PROCESS_IDENTIFIER;
    }

    @Override
    protected void validateCreateParams(CaseParams params) throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException();
        }
        GroupParams typedParams = (GroupParams) params;
        if (isTextFieldOrValueEmpty(typedParams.getName())) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected void validateUpdateParams(CaseParams params) throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException();
        }
        GroupParams typedParams = (GroupParams) params;
        if (typedParams.getName() != null && isTextFieldValueEmpty(typedParams.getName())) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected void postUpdateActions(SystemCase systemCase) {
        // none
    }

    @Override
    public Optional<Group> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return findOneByQuery(fulltextFieldQuery(GroupConstants.NAME_FIELD_ID, name));
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null) {
            return false;
        }
        return countByQuery(fulltextFieldQuery(GroupConstants.NAME_FIELD_ID, name)) > 0;
    }
}
