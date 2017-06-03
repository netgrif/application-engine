package com.netgrif.workflow.workflow.service;


import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.domain.repositories.FilterRepository;
import com.netgrif.workflow.workflow.service.interfaces.IFilterService;
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody;
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

    public List<Filter> getWithRoles(List<String> roles){
        return repository.findByRolesIn(roles);
    }

    public boolean saveFilter(LoggedUser user, CreateFilterBody filterBody){
        Filter filter = new Filter(filterBody.name);
        filter.resolveVisibility(filterBody.visibility, user);
        filter.setPetriNets(filterBody.petriNets);
        filter.setTransitions(filterBody.transitions);
        filter.setRoles(filterBody.roles);

        filter = repository.save(filter);
        return filter.get_id() != null;
    }

}
