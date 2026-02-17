package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
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
        Boolean rolePerm = userHasAtLeastOneRolePermission(user.getSelfOrImpersonated(), requestedCase.getPetriNet(), ProcessRolePermission.DELETE);
        Boolean userPerm = userHasUserListPermission(user.getSelfOrImpersonated(), requestedCase, ProcessRolePermission.DELETE);
        boolean processPerm = user.hasProcessAccess(requestedCase.getProcessIdentifier());
        return user.isAdmin() || (processPerm && (userPerm == null ? (rolePerm != null && rolePerm) : userPerm));
    }

    @Override
    public boolean canCallCreate(LoggedUser user, String netId) {
        PetriNet net = petriNetService.getPetriNet(netId);
        boolean processPerm = user.hasProcessAccess(net.getIdentifier());
        return user.isAdmin() || (processPerm && userHasAtLeastOneRolePermission(user, net, ProcessRolePermission.CREATE));
    }

    @Override
    public Boolean userHasAtLeastOneRolePermission(LoggedUser user, PetriNet net, ProcessRolePermission... permissions) {
        Map<String, Boolean> aggregatePermissions = getAggregatePermissions(user, net.getPermissions());

        for (ProcessRolePermission permission : permissions) {
            if (hasRestrictedPermission(aggregatePermissions.get(permission.toString()))) {
                return false;
            }
        }

        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(aggregatePermissions.get(permission.toString())));
    }

    @Override
    public Boolean userHasUserListPermission(LoggedUser user, Case useCase, ProcessRolePermission... permissions) {
        if (useCase.getUserRefs() == null || useCase.getUserRefs().isEmpty())
            return null;

        if (!useCase.getUsers().containsKey(user.getSelfOrImpersonatedStringId())) {
            return null;
        }

        Map<String, Boolean> userPermissions = useCase.getUsers().get(user.getSelfOrImpersonatedStringId());

        for (ProcessRolePermission permission : permissions) {
            Boolean perm = userPermissions.get(permission.toString());
            if (hasRestrictedPermission(perm)) {
                return false;
            }
        }
        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(userPermissions.get(permission.toString())));
    }
}
