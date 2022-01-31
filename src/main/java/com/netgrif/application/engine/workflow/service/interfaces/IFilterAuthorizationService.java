package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.workflow.domain.Filter;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
public interface IFilterAuthorizationService {

    boolean canCallDelete(LoggedUser user, Filter filter);

}
