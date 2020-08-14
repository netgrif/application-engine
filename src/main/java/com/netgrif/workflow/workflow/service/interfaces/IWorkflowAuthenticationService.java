package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Case;

public interface IWorkflowAuthenticationService {

    boolean canCallDelete(LoggedUser user, Case aCase);

}
