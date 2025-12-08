package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.service.UserService;
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

import java.util.*;

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
        // TODO: impersonation
//        Boolean rolePerm = userHasAtLeastOneRolePermission(userService.transformToUser((LoggedUserImpl) user.getSelfOrImpersonated()), requestedCase.getPetriNet(), ProcessRolePermission.DELETE);
//        Boolean userPerm = userHasUserListPermission(userService.transformToUser((LoggedUserImpl) user.getSelfOrImpersonated()), requestedCase, ProcessRolePermission.DELETE);
//        return user.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
        Boolean rolePerm = userHasAtLeastOneRolePermission(userService.transformToUser(user), requestedCase.getPetriNet(), ProcessRolePermission.DELETE);
        Boolean userPerm = userHasUserListPermission(userService.transformToUser(user), requestedCase, ProcessRolePermission.DELETE);
        return user.isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallCreate(LoggedUser user, String netId) {
        PetriNet net = petriNetService.getPetriNet(netId);
        // TODO: impersonation
        return user.isAdmin() || userHasAtLeastOneRolePermission(userService.transformToUser(user), net, ProcessRolePermission.CREATE);
    }

    @Override
    public Boolean userHasAtLeastOneRolePermission(AbstractUser user, PetriNet net, ProcessRolePermission... permissions) {
        Map<String, Boolean> aggregatePermissions = getAggregatePermissions(user, net.getPermissions());

        for (ProcessRolePermission permission : permissions) {
            if (hasRestrictedPermission(aggregatePermissions.get(permission.toString()))) {
                return false;
            }
        }

        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(aggregatePermissions.get(permission.toString())));
    }

    @Override
    public Boolean userHasUserListPermission(AbstractUser user, Case useCase, ProcessRolePermission... permissions) {
        if (useCase.getActorRefs() == null || useCase.getActorRefs().isEmpty()) {
            return null;
        }

        Map<String, Boolean> userPermissions = findUserPermissions(useCase, user);
        if (userPermissions == null) {
            return null;
        }

        for (ProcessRolePermission permission : permissions) {
            Boolean perm = userPermissions.get(permission.toString());
            if (hasRestrictedPermission(perm)) {
                return false;
            }
        }
        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(userPermissions.get(permission.toString())));
    }

    private Map<String, Boolean> findUserPermissions(Case useCase, AbstractUser user) {
        return findUserPermissions(useCase.getActors(), user);
    }
}
