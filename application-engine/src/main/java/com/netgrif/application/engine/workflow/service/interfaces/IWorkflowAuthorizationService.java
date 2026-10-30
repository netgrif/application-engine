package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.objects.workflow.domain.Case;

public interface IWorkflowAuthorizationService {

    boolean canCallDelete(AbstractUser user, String caseId);

    boolean canCallCreate(AbstractUser user, String netId);

    Boolean userHasAtLeastOneRolePermission(AbstractUser user, PetriNet net, ProcessRolePermission... permissions);

    Boolean userHasUserListPermission(AbstractUser user, Case useCase, ProcessRolePermission... permissions);
}
