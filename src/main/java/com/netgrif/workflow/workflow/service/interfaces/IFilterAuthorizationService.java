package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Filter;

public interface IFilterAuthorizationService {

    boolean canCallDelete(LoggedUser user, Filter filter);

}
