package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.workflow.web.TaskController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class DataGroupsResource extends CollectionModel<DataGroup> {

    public DataGroupsResource(Collection<com.netgrif.application.engine.petrinet.domain.DataGroup> content, Locale locale) {
        super(content.stream()
                .map(dg -> {
                    DataGroup dataGroup = new DataGroup(dg.getFields(), dg.getTranslatedTitle(locale), dg.getAlignment(), dg.getStretch(), dg.getLayout(), dg.getParentTaskId(), dg.getParentCaseId(), dg.getParentTaskRefId(), dg.getNestingLevel());
                    dataGroup.setParentTransitionId(dg.getParentTransitionId());
                    return dataGroup;
                })
                .collect(Collectors.toList()));
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getData("", null)).withSelfRel());
    }
}
