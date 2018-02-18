package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.workflow.domain.Filter;
import org.springframework.hateoas.ResourceAssembler;

import java.util.Locale;

public class FilterResourceAssembler implements ResourceAssembler<Filter, LocalisedFilterResource> {

    private Locale locale;

    public FilterResourceAssembler(Locale locale) {
        this.locale = locale;
    }

    @Override
    public LocalisedFilterResource toResource(Filter filter) {
        return new LocalisedFilterResource(new LocalisedFilter(filter,locale));
    }
}
