package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.workflow.domain.Task;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class TaskResourceAssembler implements RepresentationModelAssembler<Task, TaskResource> {

    public TaskResourceAssembler() {}

    @Override
    public TaskResource toModel(Task task) {
        return new TaskResource(task);
    }
}