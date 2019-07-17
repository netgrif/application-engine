package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.workflow.web.FilterController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;


public class LocalisedFilterResource extends Resource<Filter> {

    public LocalisedFilterResource(Filter content) {
        super(content, new ArrayList<Link>());
        buildLinks();
    }

    private void buildLinks() {
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(FilterController.class)
                .deleteFilter(getContent().getStringId(),null)).withRel("delete"));
    }
}
