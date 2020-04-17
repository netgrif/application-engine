package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.workflow.web.TaskController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class DataGroupsResource extends Resources<DataGroup> {

    public DataGroupsResource(Collection<com.netgrif.workflow.petrinet.domain.DataGroup> content, Locale locale) {
        super(content.stream()
                .map(dg -> new DataGroup(dg.getFields(), dg.getTranslatedTitle(locale), dg.getAlignment(), dg.getStretch(), dg.getLayout()))
                .collect(Collectors.toList()));
        buildLinks();
    }

    private void buildLinks(){
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getData("", null)).withSelfRel());
    }
}
