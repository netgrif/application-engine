package com.netgrif.workflow.workflow.web.responsebodies;

import org.springframework.hateoas.ResourceAssembler;

import java.util.Locale;

public class FilterResourceAssembler implements ResourceAssembler<com.netgrif.workflow.workflow.domain.Filter, LocalisedFilterResource> {

    private Locale locale;

    public FilterResourceAssembler(Locale locale) {
        this.locale = locale;
    }

    @Override
    public LocalisedFilterResource toResource(com.netgrif.workflow.workflow.domain.Filter filter) {
        return new LocalisedFilterResource(new Filter(filter,locale));
    }
}
