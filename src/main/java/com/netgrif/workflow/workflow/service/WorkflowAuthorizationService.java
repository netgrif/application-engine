package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowAuthorizationService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
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
        Boolean rolePerm = userHasAtLeastOneRolePermission(user.transformToUser(), requestedCase.getPetriNet(), ProcessRolePermission.DELETE);
        Boolean userPerm = userHasUserListPermission(user.transformToAnonymousUser(), requestedCase, ProcessRolePermission.DELETE);
        return user.isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallCreate(LoggedUser user, String netId) {
        PetriNet net = petriNetService.getPetriNet(netId);
        return user.isAdmin() || userHasAtLeastOneRolePermission(user.transformToUser(), net, ProcessRolePermission.CREATE);
    }

    @Override
    public Boolean userHasAtLeastOneRolePermission(IUser user, PetriNet net, ProcessRolePermission... permissions) {
        Map<String, Boolean> aggregatePermissions = getAggregatePermissions(user, net.getPermissions());

        for (ProcessRolePermission permission : permissions) {
            if (hasRestrictedPermission(aggregatePermissions.get(permission.toString()))) {
                return false;
            }
        }

        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(aggregatePermissions.get(permission.toString())));
    }

    @Override
    public Boolean userHasUserListPermission(IUser user, Case useCase, ProcessRolePermission... permissions) {
        if (useCase.getUserRefs() == null || useCase.getUserRefs().isEmpty())
            return null;

        if (!useCase.getUsers().containsKey(user.getStringId()))
            return false;

        Map<String, Boolean> userPermissions = useCase.getUsers().get(user.getStringId());

        for (ProcessRolePermission permission : permissions) {
            Boolean perm = userPermissions.get(permission.toString());
            if (hasRestrictedPermission(perm)) {
                return false;
            }
        }
        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(userPermissions.get(permission.toString())));
    }
}
