package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.service.interfaces.ICaseAuthorizationService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.petrinet.domain.Process;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CaseAuthorizationService extends AuthorizationService implements ICaseAuthorizationService {

    private final IWorkflowService workflowService;
    private final IIdentityService identityService;
    private final IPetriNetService processService;
    private final IRoleAssignmentService roleAssignmentService;

    /**
     * todo javadoc
     * */
    @Override
    public boolean canCallCreate(String processId) {
        if (processId == null) {
            return false;
        }

        Process process = processService.getPetriNet(processId);
        return canCallEvent(process.getProcessRolePermissions(), CasePermission.CREATE);
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean canCallDelete(String caseId) {
        if (caseId == null) {
            return false;
        }

        AccessPermissions<CasePermission> permissions = workflowService.findPermissionsById(caseId);
        return canCallEvent(permissions, CasePermission.DELETE);
    }

    private boolean canCallEvent(AccessPermissions<CasePermission> permissions, CasePermission permission) {
        LoggedIdentity loggedIdentity = identityService.getLoggedIdentity();
        if (loggedIdentity == null || loggedIdentity.getActiveActorId() == null) {
            return false;
        }

        Set<String> roleIds = roleAssignmentService.findAllRoleIdsByActorId(loggedIdentity.getActiveActorId());

        return !hasNegativePermission(roleIds, permissions, permission)
                && hasPositivePermission(roleIds, permissions, permission);
    }
}
