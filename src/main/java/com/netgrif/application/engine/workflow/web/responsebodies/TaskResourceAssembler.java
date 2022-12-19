package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.workflow.domain.Task;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.RepresentationModelAssembler;

@NoArgsConstructor
public class TaskResourceAssembler implements RepresentationModelAssembler<Task, TaskResource> {

    @Override
    public TaskResource toModel(Task task) {
        return new TaskResource(task);
    }
}