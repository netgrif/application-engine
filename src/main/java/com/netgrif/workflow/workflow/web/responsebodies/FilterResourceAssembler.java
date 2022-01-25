package com.netgrif.workflow.workflow.web.responsebodies;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.Locale;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
public class FilterResourceAssembler implements RepresentationModelAssembler<com.netgrif.workflow.workflow.domain.Filter, LocalisedFilterResource> {

    private Locale locale;

    public FilterResourceAssembler(Locale locale) {
        this.locale = locale;
    }

    @Override
    public LocalisedFilterResource toModel(com.netgrif.workflow.workflow.domain.Filter filter) {
        return new LocalisedFilterResource(new Filter(filter, locale));
    }
}
