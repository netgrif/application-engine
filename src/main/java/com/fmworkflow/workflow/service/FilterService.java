package com.fmworkflow.workflow.service;


import com.fmworkflow.auth.domain.LoggedUser;
import com.fmworkflow.workflow.domain.Filter;
import com.fmworkflow.workflow.domain.repositories.FilterRepository;
import com.fmworkflow.workflow.service.interfaces.IFilterService;
import com.fmworkflow.workflow.web.requestbodies.CreateFilterBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilterService implements IFilterService {

    @Autowired
    private FilterRepository repository;

    public List<Filter> getAll(){
        return repository.findAll();
    }

    public boolean saveFilter(LoggedUser user, CreateFilterBody filterBody){
        Filter filter = new Filter(filterBody.name);
        filter.resolveVisibility(filterBody.visibility, user);
        filter.setPetriNets(filterBody.petriNets);
        filter.setTransitions(filterBody.transitions);

        filter = repository.save(filter);
        return filter.get_id() != null;
    }

}
