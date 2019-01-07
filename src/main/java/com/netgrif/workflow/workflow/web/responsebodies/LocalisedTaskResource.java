package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.workflow.web.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.io.FileNotFoundException;
import java.util.ArrayList;


public class LocalisedTaskResource extends Resource<Task> {

    public LocalisedTaskResource(Task content) {
        super(content, new ArrayList<Link>());
        buildLinks();
    }

    private void buildLinks() {
        Task task = getContent();
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getOne(task.getStringId(), null)).withSelfRel());
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .assign(null, task.getStringId())).withRel("assign"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .delegate(null, task.getStringId(), null)).withRel("delegate"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .finish(null, task.getStringId())).withRel("finish"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .cancel(null, task.getStringId())).withRel("cancel"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getData(task.getStringId(), null)).withRel("data"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .saveData(task.getStringId(), null)).withRel("data-edit"));
        try {
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getFile(task.getStringId(), "", null)).withRel("file"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}