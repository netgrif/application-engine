package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.service.interfaces.ICaseAuthorizationService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.petrinet.domain.Process;
import org.springframework.stereotype.Service;

@Service
public class CaseAuthorizationService extends AuthorizationService implements ICaseAuthorizationService {

    private final IWorkflowService workflowService;
    private final IPetriNetService processService;

    public CaseAuthorizationService(IIdentityService identityService, IRoleAssignmentService roleAssignmentService,
                                    ApplicationRoleRunner applicationRoleRunner, IWorkflowService workflowService,
                                    IPetriNetService processService) {
        super(identityService, roleAssignmentService, applicationRoleRunner);
        this.workflowService = workflowService;
        this.processService = processService;
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean canCallCreate(String processId) {
        if (processId == null) {
            return false;
        }

        Process process = processService.getPetriNet(processId);
        return canCallEvent(process.getProcessRolePermissions(), new AccessPermissions<>(), CasePermission.CREATE);
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean canCallDelete(String caseId) {
        if (caseId == null) {
            return false;
        }

        Case targetCase = workflowService.findOne(caseId);
        return canCallEvent(targetCase.getProcessRolePermissions(), targetCase.getCaseRolePermissions(), CasePermission.CREATE);
    }
}
