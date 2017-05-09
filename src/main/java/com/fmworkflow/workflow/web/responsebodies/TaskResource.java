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
                .getOne(task.getStringId())).withSelfRel());
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .assign(auth,task.getStringId())).withRel("assign"));
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .delegate(auth,task.getStringId(),null)).withRel("delegate"));
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .finish(auth,task.getStringId())).withRel("finish"));
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .cancel(auth,task.getStringId())).withRel("cancel"));
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getData(task.getStringId())).withRel("data"));
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .saveData(task.getStringId(),null)).withRel("data-edit"));
        resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getFile(task.getStringId(),"",null)).withRel("file"));
        return resource;
    }
}
