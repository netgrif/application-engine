package com.netgrif.application.engine.workflow.service.interfaces;


import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.application.engine.workflow.domain.Filter;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import com.netgrif.application.engine.workflow.web.requestbodies.CreateFilterBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
public interface IFilterService {

    boolean deleteFilter(String filterId, LoggedUser user) throws UnauthorisedRequestException;

    Filter saveFilter(CreateFilterBody newFilterBody, MergeFilterOperation operation, LoggedUser user);

    Page<Filter> search(Map<String, Object> request, Pageable pageable, LoggedUser user);
}
