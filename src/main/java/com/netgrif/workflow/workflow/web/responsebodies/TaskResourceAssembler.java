package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.Locale;

public class TaskResourceAssembler implements RepresentationModelAssembler<Task, LocalisedTaskResource> {

    private Locale locale;

    public TaskResourceAssembler(Locale locale) {
        this.locale = locale;
    }

    @Override
    public LocalisedTaskResource toModel(com.netgrif.workflow.workflow.domain.Task task) {
        return new LocalisedTaskResource(new com.netgrif.workflow.workflow.web.responsebodies.Task(task, locale));
    }
}