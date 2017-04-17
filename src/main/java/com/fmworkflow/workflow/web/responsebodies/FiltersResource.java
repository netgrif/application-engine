package com.fmworkflow.workflow.web.responsebodies;

import com.fmworkflow.workflow.domain.Filter;
import com.fmworkflow.workflow.web.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;


public class FiltersResource extends Resources<Filter> {

    public FiltersResource(Iterable<Filter> content, int method) {
        super(content, new ArrayList<Link>());
        buildLinks(method);
    }

    private void buildLinks(int method) {
        ControllerLinkBuilder allLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAllFilters(null));
        ControllerLinkBuilder rolesLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getFiltersWithRoles(new ArrayList<>()));
        if(method == 0){
            add(allLink.withSelfRel());
            add(rolesLink.withRel("roles"));
        } else if(method == 1){
            add(allLink.withRel("all"));
            add(rolesLink.withSelfRel());
        }
    }
}
