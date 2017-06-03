package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.web.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;


public class FiltersResource extends Resources<Filter> {

    public FiltersResource(Iterable<Filter> content, boolean withRoles) {
        super(content, new ArrayList<Link>());
        buildLinks(withRoles);
    }

    private void buildLinks(boolean withRoles) {
        ControllerLinkBuilder allLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAllFilters(null));
        ControllerLinkBuilder rolesLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getFiltersWithRoles(new ArrayList<>()));
        if(withRoles){
            add(allLink.withRel("all"));
            add(rolesLink.withSelfRel());
        } else {
            add(allLink.withSelfRel());
            add(rolesLink.withRel("roles"));
        }
    }
}
