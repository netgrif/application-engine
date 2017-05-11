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
        buildLinks();
    }

    private void buildLinks(){
        Task task = getContent();
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getOne(task.getStringId())).withSelfRel());
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .assign(null,task.getStringId())).withRel("assign"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .delegate(null,task.getStringId(),null)).withRel("delegate"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .finish(null,task.getStringId())).withRel("finish"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .cancel(null,task.getStringId())).withRel("cancel"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getData(task.getStringId())).withRel("data"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .saveData(task.getStringId(),null)).withRel("data-edit"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getFile(task.getStringId(),"",null)).withRel("file"));
    }
}
