package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.workflow.web.TaskController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class DataGroupsResource extends Resources<LocalisedDataGroup> {

    public DataGroupsResource(Collection<DataGroup> content, Locale locale) {
        super(content.stream()
                .map(dg -> new LocalisedDataGroup(dg.getFields(), dg.getTitle().getTranslation(locale), dg.getAlignment(), dg.getStretch()))
                .collect(Collectors.toList()));
        buildLinks();
    }

    private void buildLinks(){
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getData("", null)).withSelfRel());
    }
}
