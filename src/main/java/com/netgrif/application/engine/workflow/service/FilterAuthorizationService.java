package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.AuthorizingObject;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.workflow.domain.Filter;
import com.netgrif.application.engine.workflow.service.interfaces.IFilterAuthorizationService;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
@Service
public class FilterAuthorizationService implements IFilterAuthorizationService {

    @Override
    public boolean canCallDelete(LoggedUser user, Filter filter) {
        return user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(AuthorizingObject.FILTER_DELETE_ALL.name())) || user.getId().equals(filter.getAuthor().getId());
    }
}
