package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.startup.DefaultGroupRunner;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupService extends ActorService<Group> implements IGroupService {

    public GroupService(ISessionManagerService sessionManagerService, IDataService dataService,
                        IWorkflowService workflowService, SystemCaseFactoryRegistry systemCaseFactory,
                        IElasticCaseSearchService elasticCaseSearchService, @Lazy DefaultGroupRunner defaultGroupRunner) {
        super(sessionManagerService, dataService, workflowService, systemCaseFactory, elasticCaseSearchService,
                defaultGroupRunner);
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

    @Override
    protected String getProcessIdentifier() {
        return GroupConstants.PROCESS_IDENTIFIER;
    }

    @Override
    protected void validateAndFixCreateParams(CaseParams params) throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for group");
        }
        GroupParams typedParams = (GroupParams) params;
        if (isTextFieldOrValueEmpty(typedParams.getName())) {
            throw new IllegalArgumentException("Group must have a name");
        }
        if (typedParams.getName().getRawValue().equals(GroupConstants.DEFAULT_GROUP_NAME)) {
            throw new IllegalArgumentException(String.format("Group name [%s] is reserved by system.",
                    GroupConstants.DEFAULT_GROUP_NAME));
        }

        if (isDefaultGroupByName(typedParams.getName().getRawValue())
                && isCaseFieldOrValueEmpty(typedParams.getParentGroupId())) {
            typedParams.setParentGroupId(CaseField.withValue(List.of(getDefaultGroup().getStringId())));
        }
    }

    @Override
    protected void validateAndFixUpdateParams(CaseParams params) throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for group");
        }
        GroupParams typedParams = (GroupParams) params;
        if (typedParams.getName() != null && isTextFieldValueEmpty(typedParams.getName())) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isDefaultGroupByName(String name) {
        if (name == null) {
            return false;
        }
        return name.equals(GroupConstants.DEFAULT_GROUP_NAME);
    }
}
