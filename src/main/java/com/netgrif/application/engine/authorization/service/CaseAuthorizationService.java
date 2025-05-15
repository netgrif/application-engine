package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.service.interfaces.*;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
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

    public CaseAuthorizationService(ISessionManagerService sessionManagerService, IRoleAssignmentService roleAssignmentService,
                                    ApplicationRoleRunner applicationRoleRunner, IWorkflowService workflowService,
                                    IPetriNetService processService) {
        super(sessionManagerService, applicationRoleRunner, roleAssignmentService);
        this.workflowService = workflowService;
        this.processService = processService;
    }

    /**
     * Checks if the current actor has permission to create a case for a given process.
     * @param processId identifier of the process to check create permissions for
     * @return true if an actor has permission to create case, false otherwise
     */
    @Override
    public boolean canCallCreate(String processId) {
        if (processId == null) {
            return false;
        }

        Process process = processService.getPetriNet(processId);
        return canCallEvent(process.getProcessRolePermissions(), new AccessPermissions<>(), CasePermission.CREATE);
    }

    /**
     * Checks if the current actor has permission to delete a specified case.
     * @param caseId identifier of the case to check delete permissions for
     * @return true if an actor has permission to delete case, false otherwise
     */
    @Override
    public boolean canCallDelete(String caseId) {
        if (caseId == null) {
            return false;
        }

        Case targetCase = workflowService.findOne(caseId);
        return canCallEvent(targetCase.getProcessRolePermissions(), targetCase.getCaseRolePermissions(), CasePermission.DELETE);
    }

    /**
     * Checks if the current actor has permission to view a specified case.
     * @param caseId identifier of the case to check view permissions for
     * @return true if an actor has permission to view case, false otherwise
     */
    @Override
    public boolean canView(String caseId) {
        // todo 2058 unit test
        if (caseId == null) {
            return false;
        }

        Case targetCase = workflowService.findOne(caseId);
        return canCallEvent(targetCase.getProcessRolePermissions(), targetCase.getCaseRolePermissions(), CasePermission.VIEW);
    }
}
