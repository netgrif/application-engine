package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.web.FilterController;
import com.netgrif.workflow.workflow.web.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;


public class LocalisedFilterResource extends Resource<LocalisedFilter> {

    public LocalisedFilterResource(LocalisedFilter content) {
        super(content, new ArrayList<Link>());
        buildLinks();
    }

    private void buildLinks() {
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(FilterController.class)
                .deleteFilter(getContent().getStringId(),null)).withRel("delete"));
    }
}
