package com.netgrif.application.engine.workflow.service;

import com.netgrif.adapter.auth.domain.LoggedUserImpl;
import com.netgrif.auth.service.UserService;
import com.netgrif.core.auth.domain.IUser;
import com.netgrif.core.auth.domain.LoggedUser;
import com.netgrif.core.petrinet.domain.PetriNet;
import com.netgrif.core.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.core.workflow.domain.Case;
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

    @Autowired
    private UserService userService;

    @Override
    public boolean canCallDelete(LoggedUser user, String caseId) {
        Case requestedCase = workflowService.findOne(caseId);
        Boolean rolePerm = userHasAtLeastOneRolePermission(userService.transformToUser((LoggedUserImpl) user.getSelfOrImpersonated()), requestedCase.getPetriNet(), ProcessRolePermission.DELETE);
        Boolean userPerm = userHasUserListPermission(userService.transformToUser((LoggedUserImpl) user.getSelfOrImpersonated()), requestedCase, ProcessRolePermission.DELETE);
        return user.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallCreate(LoggedUser user, String netId) {
        PetriNet net = petriNetService.getPetriNet(netId);
        return user.getSelfOrImpersonated().isAdmin() || userHasAtLeastOneRolePermission(userService.transformToUser((LoggedUserImpl) user.getSelfOrImpersonated()), net, ProcessRolePermission.CREATE);
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

        if (!useCase.getUsers().containsKey(user.getSelfOrImpersonated().getStringId()))
            return null;

        Map<String, Boolean> userPermissions = useCase.getUsers().get(user.getSelfOrImpersonated().getStringId());

        for (ProcessRolePermission permission : permissions) {
            Boolean perm = userPermissions.get(permission.toString());
            if (hasRestrictedPermission(perm)) {
                return false;
            }
        }
        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(userPermissions.get(permission.toString())));
    }
}
