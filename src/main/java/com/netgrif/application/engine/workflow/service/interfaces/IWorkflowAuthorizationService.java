package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.authentication.domain.IUser;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.workflow.domain.Case;

public interface IWorkflowAuthorizationService {

    boolean canCallDelete(Identity user, String caseId);

    boolean canCallCreate(Identity user, String netId);

    Boolean userHasAtLeastOneRolePermission(IUser user, Process net, CasePermission... permissions);

    Boolean userHasUserListPermission(IUser user, Case useCase, CasePermission... permissions);
}
