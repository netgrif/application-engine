package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowAuthenticationService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowAuthenticationService implements IWorkflowAuthenticationService {

    @Override
    public boolean canCallDelete(LoggedUser user, Case aCase) {
        return user.isAdmin() || user.getId().equals(aCase.getAuthor().getId());
    }
}
