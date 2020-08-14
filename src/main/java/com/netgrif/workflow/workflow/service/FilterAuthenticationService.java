package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.service.interfaces.IFilterAuthenticationService;
import org.springframework.stereotype.Service;

@Service
public class FilterAuthenticationService implements IFilterAuthenticationService {

    @Override
    public boolean canCallDelete(LoggedUser user, Filter filter) {
        return user.isAdmin() || user.getId().equals(filter.getAuthor().getId());
    }
}
