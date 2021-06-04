package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.service.interfaces.IFilterAuthorizationService;
import org.springframework.stereotype.Service;

/**
 * @deprecated
 * since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
@Service
public class FilterAuthorizationService implements IFilterAuthorizationService {

    @Override
    public boolean canCallDelete(LoggedUser user, Filter filter) {
        return user.isAdmin() || user.getId().equals(filter.getAuthor().getId());
    }
}
