package com.fmworkflow.workflow.web.responsebodies;

import com.fmworkflow.workflow.domain.Task;
import com.fmworkflow.workflow.web.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.lang.reflect.Method;
import java.util.ArrayList;


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
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .cancel(auth,task.getId())).withRel("cancel"));
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getData(task.getId())).withRel("data"));
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .saveData(task.getId(),null)).withRel("data-edit"));
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getFile(task.getId(),"")).withRel("file"));
        return resource;
    }
}
