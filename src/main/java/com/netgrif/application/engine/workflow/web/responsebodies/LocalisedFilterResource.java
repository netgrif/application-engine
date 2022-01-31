package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.application.engine.workflow.web.FilterController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
public class LocalisedFilterResource extends EntityModel<Filter> {

    public LocalisedFilterResource(Filter content) {
        super(content, new ArrayList<Link>());
        buildLinks();
    }

    private void buildLinks() {
        try {
            add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(FilterController.class)
                    .deleteFilter(getContent().getStringId(), null)).withRel("delete"));
        } catch (UnauthorisedRequestException e) {
            e.printStackTrace();
        }
    }
}
