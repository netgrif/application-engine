package com.netgrif.workflow.workflow.service.interfaces;


import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IFilterService {

    boolean deleteFilter(String filterId, LoggedUser user);

    Filter saveFilter(CreateFilterBody newFilterBody, LoggedUser user);

    Page<Filter> search(Map<String, Object> request, Pageable pageable, LoggedUser user);

	Filter findOne(String filterId);
}
