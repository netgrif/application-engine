package com.fmworkflow.workflow.domain;

import com.fmworkflow.workflow.web.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;

/**
 * Created by Milan on 9.2.2017.
 */
public class TaskResource extends Resource<Task> {

    public TaskResource(Task content) {
        super(content, new ArrayList<Link>());
    }

    public static TaskResource createFrom(Task task, Authentication auth){
        TaskResource resource = new TaskResource(task);

        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getOne(task.getId())).withSelfRel());
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .assign(auth,task.getId())).withRel("assign"));
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .finish(auth,task.getId())).withRel("finish"));

        return resource;
    }
}
