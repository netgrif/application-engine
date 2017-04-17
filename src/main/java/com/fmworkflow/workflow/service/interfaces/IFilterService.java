package com.fmworkflow.workflow.service.interfaces;


import com.fmworkflow.auth.domain.LoggedUser;
import com.fmworkflow.workflow.domain.Filter;
import com.fmworkflow.workflow.web.requestbodies.CreateFilterBody;

import java.util.List;

public interface IFilterService {

    List<Filter> getAll();
    List<Filter> getWithRoles(List<String> roles);
    boolean saveFilter(LoggedUser user, CreateFilterBody filterBody);
}
