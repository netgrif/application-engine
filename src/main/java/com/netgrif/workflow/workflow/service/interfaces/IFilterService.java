package com.netgrif.workflow.workflow.service.interfaces;


import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody;

import java.util.List;

public interface IFilterService {

    List<Filter> getAll();
    List<Filter> getWithRoles(List<String> roles);
    boolean saveFilter(LoggedUser user, CreateFilterBody filterBody);
}
