package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.startup.DefaultGroupRunner;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class GroupService extends ActorService<Group> implements IGroupService {

    private final DefaultGroupRunner defaultGroupRunner;
    private final IRoleService roleService;

    public GroupService(ISessionManagerService sessionManagerService, IDataService dataService,
                        IWorkflowService workflowService, SystemCaseFactoryRegistry systemCaseFactory,
                        IElasticCaseSearchService elasticCaseSearchService, @Lazy DefaultGroupRunner defaultGroupRunner,
                        IRoleService roleService) {
        super(sessionManagerService, dataService, workflowService, systemCaseFactory, elasticCaseSearchService);
        this.defaultGroupRunner = defaultGroupRunner;
        this.roleService = roleService;
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
    public Group getDefaultGroup() {
        Group defaultGroup = defaultGroupRunner.getDefaultGroup();

        if (defaultGroup == null) {
            defaultGroup = doCreate(GroupParams.with()
                    .name(new TextField(GroupConstants.DEFAULT_GROUP_NAME))
                    .build(), null);
            log.info("Default group with id [{}] was created.", defaultGroup.getStringId());
        }

        roleService.assignRolesToActor(defaultGroup.getStringId(), Set.of(roleService.findDefaultRole().getStringId()));

        return defaultGroup;
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
        if (isForbidden(typedParams.getName().getRawValue())) {
            throw new IllegalArgumentException(String.format("Group name [%s] is reserved by system.",
                    typedParams.getName().getRawValue()));
        }

        if (isCaseFieldOrValueEmpty(typedParams.getParentGroupId())) {
            typedParams.setParentGroupId(CaseField.withValue(List.of(getDefaultGroup().getStringId())));
        }
    }

    @Override
    protected void validateAndFixUpdateParams(CaseParams params) throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input values for group");
        }
        GroupParams typedParams = (GroupParams) params;
        if (typedParams.getName() == null) {
            return;
        }
        if (isTextFieldValueEmpty(typedParams.getName())) {
            throw new IllegalArgumentException();
        }
        if (isForbidden(typedParams.getName().getRawValue())) {
            throw new IllegalArgumentException(String.format("Group name [%s] is reserved by system.",
                    typedParams.getName().getRawValue()));
        }
    }
}
