package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.objects.workflow.domain.Case;

public interface IWorkflowAuthorizationService {

    boolean canCallDelete(LoggedUser user, String caseId);

    boolean canCallCreate(LoggedUser user, String netId);

    Boolean userHasAtLeastOneRolePermission(IUser user, PetriNet net, ProcessRolePermission... permissions);

    Boolean userHasUserListPermission(IUser user, Case useCase, ProcessRolePermission... permissions);
}
