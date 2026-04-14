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
        String taskId = content.stream()
                .map(com.netgrif.application.engine.petrinet.domain.DataGroup::getParentTaskId)
                .filter(id -> id != null && !id.isBlank())
                .findFirst()
                .orElse(null);
        buildLinks(taskId);
    }

    private void buildLinks(String taskId) {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getData(taskId, null, null)).withSelfRel());
    }
}
