package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.workflow.web.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class DataFieldsResource extends Resources<LocalisedField> {

    public DataFieldsResource(Collection<Field> content, String taskId, Locale locale) {
        super(content.stream()
                .map(f -> new LocalisedField(f, locale))
                .collect(Collectors.toList()), new ArrayList<Link>());
        buildLinks(taskId);
    }

    private void buildLinks(String taskId) {
        if (taskId == null) return;

        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getData(taskId, null)).withSelfRel());
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .saveData(taskId, null)).withRel("edit"));
    }
}