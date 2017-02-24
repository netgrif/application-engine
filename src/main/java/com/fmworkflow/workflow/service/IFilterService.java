package com.fmworkflow.workflow.service;


import com.fmworkflow.auth.domain.LoggedUser;
import com.fmworkflow.workflow.domain.Filter;
import com.fmworkflow.workflow.web.requestbodies.CreateFilterBody;

import java.util.List;

public interface IFilterService {

    List<Filter> getAll();
    boolean saveFilter(LoggedUser user, CreateFilterBody filterBody);
}
