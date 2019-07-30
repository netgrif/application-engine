package com.netgrif.workflow.workflow.service;


import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.Filter;
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

    @Override
    public boolean deleteFilter(String filterId, LoggedUser user) {
        Optional<Filter> result = repository.findById(filterId);
        if (!result.isPresent())
            throw new IllegalArgumentException("Filter not found");

        Filter filter = result.get();
        if (filter.getAuthor().getId().equals(user.getId()))
            throw new IllegalArgumentException("User " + user.getUsername() + " doesn't have permission to delete filter " + filter.getStringId());

        repository.delete(filter);
        return true;
    }

    @Override
    public Filter saveFilter(CreateFilterBody newFilterBody, LoggedUser user) {
        Filter filter = new Filter();
        filter.setAuthor(user.transformToAuthor());
        filter.setTitle(new I18nString(newFilterBody.getTitle()));
        filter.setDescription(new I18nString(newFilterBody.getDescription()));
        filter.setType(newFilterBody.getType());
        filter.setVisibility(newFilterBody.getVisibility());
        filter.setQuery(newFilterBody.getQuery());

        return repository.save(filter);
    }

    @Override
    public Page<Filter> search(Map<String, Object> request, Pageable pageable, LoggedUser user) {
        if (request.containsKey("visibility") && request.get("visibility").equals(Filter.VISIBILITY_PRIVATE)) {
            request.put("author", user.getId());
        }

        return searchService.search(request, pageable, Filter.class);
    }
}
