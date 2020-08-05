package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.workflow.workflow.web.TaskController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.io.FileNotFoundException;
import java.util.ArrayList;


public class LocalisedTaskResource extends Resource<Task> {

    public static final Logger log = LoggerFactory.getLogger(LocalisedTaskResource.class);

    public LocalisedTaskResource(Task content) {
        super(content, new ArrayList<Link>());
        buildLinks();
    }

    private void buildLinks() {
        Task task = getContent();
        try {
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
        } catch (UnauthorisedRequestException e) {
            e.printStackTrace();
        }
        try {
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getFile(task.getStringId(), "", null)).withRel("file"));
        } catch (FileNotFoundException e) {
            log.error("Building links failed: ", e);
        }
    }
}