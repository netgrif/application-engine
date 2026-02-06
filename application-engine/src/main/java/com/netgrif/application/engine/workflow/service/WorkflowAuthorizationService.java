package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.objects.workflow.domain.Case;
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
        // TODO: impersonation user.getSelfOrImpersonated().isAdmin()
        if (user.isAdmin()) {
            return true;
        }

        Case requestedCase = workflowService.findOne(caseId);
        // TODO: impersonation user.getSelfOrImpersonated()
        Boolean userPerm = userHasUserListPermission(user, requestedCase, ProcessRolePermission.DELETE);
        if (userPerm != null) {
            return userPerm;
        }

        // TODO: impersonation user.getSelfOrImpersonated()
        Boolean rolePerm = userHasAtLeastOneRolePermission(user, requestedCase.getPetriNet(), ProcessRolePermission.DELETE);
        return rolePerm != null && rolePerm;
    }

    @Override
    public boolean canCallCreate(LoggedUser user, String netId) {
        // TODO: impersonation
        if (user.isAdmin()) {
            return true;
        }

        PetriNet net = petriNetService.getPetriNet(netId);
        // TODO: impersonation
        return userHasAtLeastOneRolePermission(user, net, ProcessRolePermission.CREATE);
    }

    @Override
    public Boolean userHasAtLeastOneRolePermission(AbstractUser user, PetriNet net, ProcessRolePermission... permissions) {
        Map<String, Boolean> aggregatePermissions = getAggregatePermissions(user, net.getPermissions());

        for (ProcessRolePermission permission : permissions) {
            if (hasRestrictedPermission(aggregatePermissions.get(permission.toString()))) {
                return false;
            }
        }

        return Arrays.stream(permissions)
                .anyMatch(permission -> hasPermission(aggregatePermissions.get(permission.toString())));
    }

    @Override
    public Boolean userHasUserListPermission(AbstractUser user, Case useCase, ProcessRolePermission... permissions) {
        if (useCase.getUserRefs() == null || useCase.getUserRefs().isEmpty())
            return null;

        // TODO: impersonation user.getSelfOrImpersonated().getStringId()
        if (!useCase.getUsers().containsKey(user.getStringId())) {
            return null;
        }

        // TODO: impersonation
        Map<String, Boolean> userPermissions = useCase.getUsers().get(user.getStringId());

        for (ProcessRolePermission permission : permissions) {
            Boolean perm = userPermissions.get(permission.toString());
            if (hasRestrictedPermission(perm)) {
                return false;
            }
        }
        return Arrays.stream(permissions)
                .anyMatch(permission -> hasPermission(userPermissions.get(permission.toString())));
    }
}
