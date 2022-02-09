package com.netgrif.application.engine.workflow.service;


import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Filter;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import com.netgrif.application.engine.workflow.domain.repositories.FilterRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IFilterService;
import com.netgrif.application.engine.workflow.web.requestbodies.CreateFilterBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
@Service
public class FilterService implements IFilterService {

    @Autowired
    private FilterRepository repository;

    @Autowired
    private FilterSearchService searchService;

    @Autowired
    private FilterAuthorizationService authenticationService;

    @Override
    public boolean deleteFilter(String filterId, LoggedUser user) throws UnauthorisedRequestException {
        Optional<Filter> result = repository.findById(filterId);
        if (!result.isPresent())
            throw new IllegalArgumentException("Filter not found");

        Filter filter = result.get();
        if (!authenticationService.canCallDelete(user, filter))
            throw new UnauthorisedRequestException("User " + user.getUsername() + " doesn't have permission to delete filter " + filter.getStringId());

        repository.delete(filter);
        return true;
    }

    @Override
    public Filter saveFilter(CreateFilterBody newFilterBody, MergeFilterOperation operation, LoggedUser user) {
        Filter filter = new Filter();
        filter.setAuthor(user.transformToAuthor());
        filter.setTitle(new I18nString(newFilterBody.getTitle()));
        filter.setDescription(new I18nString(newFilterBody.getDescription()));
        filter.setType(newFilterBody.getType());
        filter.setVisibility(newFilterBody.getVisibility());
        filter.setMergeOperation(operation);
        filter.setQuery(newFilterBody.getQuery());

        return repository.save(filter);
    }

    @Override
    public Page<Filter> search(Map<String, Object> request, Pageable pageable, LoggedUser user) {
        if (request.containsKey("visibility") && request.get("visibility").equals(Filter.VISIBILITY_PRIVATE)) {
            request.put("author", user.getId());
        }

        return repository.findAll(searchService.buildQuery(request, user, null), pageable);
    }
}
