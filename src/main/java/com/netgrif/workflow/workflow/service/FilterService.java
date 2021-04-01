package com.netgrif.workflow.workflow.service;


import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.domain.FilterType;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.domain.repositories.FilterRepository;
import com.netgrif.workflow.workflow.service.interfaces.IFilterService;
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

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
        Filter filter;
        Author author = user.transformToAuthor();
        I18nString title = new I18nString(newFilterBody.getTitle());
        I18nString desc = new I18nString(newFilterBody.getDescription());
        FilterType type = FilterType.resolveType(newFilterBody.getType());
        // TODO visibility
        if (type == FilterType.CASE && newFilterBody.getCaseFilterBodies() != null) {
            filter = Filter.createCaseFilter(title, desc, Filter.VISIBILITY_PUBLIC, author, newFilterBody.getMergeOperator(), newFilterBody.getCaseFilterBodies());
        } else if (type == FilterType.TASK && newFilterBody.getTaskFilterBodies() != null) {
            filter = Filter.createTaskFilter(title, desc, Filter.VISIBILITY_PUBLIC, author, newFilterBody.getMergeOperator(), newFilterBody.getTaskFilterBodies());
        } else {
            throw new IllegalArgumentException("Filter type must be either 'Task' or 'Case' and the corresponding filter bodies must be defined!");
        }

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
