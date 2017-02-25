package com.fmworkflow.workflow.web.responsebodies;

import com.fmworkflow.workflow.web.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;


public class TasksResource extends Resources<TaskResource> {
    public TasksResource(Iterable<TaskResource> content) {
        super(content, new ArrayList<Link>());
    }

    public void addLinks(String selfRel){
        if(selfRel.equals("all"))
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
            .getAll(null)).withSelfRel());
        else
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getAll(null)).withRel("all"));

        if(selfRel.equals("my"))
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getMy(null)).withSelfRel());
        else
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getMy(null)).withRel("my"));

        if(selfRel.equals("finished"))
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getMyFinished(null)).withSelfRel());
        else
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getMyFinished(null)).withRel("finished"));

        if(selfRel.equals("search"))
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .search(null, null)).withSelfRel());
        else
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .search(null, null)).withRel("search"));
    }
}
