package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.workflow.workflow.domain.Case;

public interface IWorkflowAuthorizationService {

    boolean canCallDelete(LoggedUser user, String caseId);

    boolean canCallCreate(LoggedUser user, String netId);

    boolean userHasAtLeastOneRolePermission(IUser user, PetriNet net, ProcessRolePermission... permissions);

    boolean userHasUserListPermission(IUser user, Case useCase, ProcessRolePermission... permissions);
}
