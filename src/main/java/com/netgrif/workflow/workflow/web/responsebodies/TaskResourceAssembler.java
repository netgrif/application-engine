package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.hateoas.ResourceAssembler;

import java.util.Locale;

public class TaskResourceAssembler implements ResourceAssembler<Task, LocalisedTaskResource> {

    private Locale locale;

    public TaskResourceAssembler(Locale locale) {
        this.locale = locale;
    }

    @Override
    public LocalisedTaskResource toResource(com.netgrif.workflow.workflow.domain.Task task) {
        return new LocalisedTaskResource(new com.netgrif.workflow.workflow.web.responsebodies.Task(task, locale));
    }
}