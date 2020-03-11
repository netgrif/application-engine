package com.netgrif.workflow.workflow.service.interfaces;


import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IFilterService {

    boolean deleteFilter(String filterId, LoggedUser user) throws UnauthorisedRequestException;

    Filter saveFilter(CreateFilterBody newFilterBody, MergeFilterOperation operation, LoggedUser user);

    Page<Filter> search(Map<String, Object> request, Pageable pageable, LoggedUser user);
}
