package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Filter;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
public interface IFilterAuthorizationService {

    boolean canCallDelete(LoggedUser user, Filter filter);

}
