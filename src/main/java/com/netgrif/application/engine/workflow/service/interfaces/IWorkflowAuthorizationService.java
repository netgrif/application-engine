package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.roles.CasePermission;
import com.netgrif.application.engine.workflow.domain.Case;

public interface IWorkflowAuthorizationService {

    boolean canCallDelete(LoggedUser user, String caseId);

    boolean canCallCreate(LoggedUser user, String netId);

    Boolean userHasAtLeastOneRolePermission(IUser user, Process net, CasePermission... permissions);

    Boolean userHasUserListPermission(IUser user, Case useCase, CasePermission... permissions);
}
