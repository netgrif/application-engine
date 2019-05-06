package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.workflow.web.FilterController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.util.ArrayList;


public class LocalisedFilterResource extends Resource<LocalisedFilter> {

    public LocalisedFilterResource(LocalisedFilter content) {
        super(content, new ArrayList<Link>());
        buildLinks();
    }

    private void buildLinks() {
        Method method;
        try {
            method = FilterController.class.getMethod("deleteFilter", String.class, Authentication.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        add(ControllerLinkBuilder.linkTo(method, getContent().getStringId(), null).withRel("delete"));
    }
}
