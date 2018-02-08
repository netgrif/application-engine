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

import java.util.List;
import java.util.Map;

@Service
public class FilterService implements IFilterService {

    @Autowired
    private FilterRepository repository;

    @Override
    public boolean deleteFilter(String filterId, LoggedUser user) {
        Filter filter = repository.findOne(filterId);
        if(filter == null)
            return false;

        if(filter.getAuthor().getId().equals(user.getId()))
            return false;

        repository.delete(filter);
        return true;
    }

    @Override
    public boolean saveFilter(CreateFilterBody newFilterBody, LoggedUser user) {
        Filter filter = new Filter();
        filter.setAuthor(user.transformToAuthor());
        filter.setTitle(new I18nString(newFilterBody.getTitle()));
        filter.setDescription(new I18nString(newFilterBody.getDescription()));
        filter.setType(newFilterBody.getType());
        filter.setVisibility(newFilterBody.getVisibility());
        filter.setQuery(newFilterBody.getQuery());

        filter = repository.save(filter);
        return filter.get_id() != null;
    }

    @Override
    public Page<Filter> search(Map<String, Object> request, Pageable pageable, LoggedUser user) {
        return null;
    }
}
