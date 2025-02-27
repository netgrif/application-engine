package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowAuthorizationService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
public class WorkflowAuthorizationService extends AbstractAuthorizationService implements IWorkflowAuthorizationService {

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IPetriNetService petriNetService;

    @Override
    public boolean canCallDelete(LoggedUser user, String caseId) {
        Case requestedCase = workflowService.findOne(caseId);
        Boolean rolePerm = userHasAtLeastOneRolePermission(user.getSelfOrImpersonated().transformToUser(), requestedCase.getProcess(), CasePermission.DELETE);
        Boolean userPerm = userHasUserListPermission(user.transformToUser(), requestedCase, CasePermission.DELETE);
        return user.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallCreate(LoggedUser user, String netId) {
        Process net = petriNetService.getPetriNet(netId);
        return user.getSelfOrImpersonated().isAdmin() || userHasAtLeastOneRolePermission(user.transformToUser(), net, CasePermission.CREATE);
    }

    @Override
    public Boolean userHasAtLeastOneRolePermission(IUser user, Process net, CasePermission... permissions) {
        Map<CasePermission, Boolean> aggregatePermissions = getAggregateRoleCasePermissions(user, net.getProcessRolePermissions());

        for (CasePermission permission : permissions) {
            if (hasRestrictedPermission(aggregatePermissions.get(permission))) {
                return false;
            }
        }

        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(aggregatePermissions.get(permission)));
    }

    @Override
    public Boolean userHasUserListPermission(IUser user, Case useCase, CasePermission... permissions) {
//        TODO: release/8.0.0
//        if (useCase.getUserRefs() == null || useCase.getUserRefs().isEmpty()) {
//            return null;
//        }
//
//        if (!useCase.getUsers().containsKey(user.getSelfOrImpersonated().getStringId())) {
//            return null;
//        }
//        Map<ProcessRolePermission, Boolean> userPermissions = useCase.getUsers().get(user.getSelfOrImpersonated().getStringId());
//        for (ProcessRolePermission permission : permissions) {
//            Boolean perm = userPermissions.get(permission);
//            if (hasRestrictedPermission(perm)) {
//                return false;
//            }
//        }
//        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(userPermissions.get(permission)));
        return true;
    }
}
