package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowAuthorizationService implements IWorkflowAuthorizationService {

    @Override
    public boolean canCallDelete(LoggedUser user, Case aCase) {
        return user.isAdmin() || user.getId().equals(aCase.getAuthor().getId());
    }
}
