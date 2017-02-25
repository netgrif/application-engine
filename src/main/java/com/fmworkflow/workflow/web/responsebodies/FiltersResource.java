package com.fmworkflow.workflow.web.responsebodies;

import com.fmworkflow.workflow.domain.Filter;
import com.fmworkflow.workflow.web.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;


public class FiltersResource extends Resources<Filter> {

    public FiltersResource(Iterable<Filter> content) {
        super(content, new ArrayList<Link>());
        buildLinks();
    }

    private void buildLinks() {
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAllFilters(null)).withSelfRel());
    }
}
