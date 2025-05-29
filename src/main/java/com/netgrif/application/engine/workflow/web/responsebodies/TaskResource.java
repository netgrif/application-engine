package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.web.TaskController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.security.core.Authentication;

import java.io.FileNotFoundException;
import java.util.ArrayList;


@Slf4j
public class TaskResource extends EntityModel<Task> {

    public TaskResource(Task content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks() {
        Task task = getContent();
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getOne(task.getStringId(), null)).withSelfRel());
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .assign((Authentication) null, task.getStringId())).withRel("assign"));
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .delegate((Authentication) null, task.getStringId(), null)).withRel("delegate"));
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .finish((Authentication) null, task.getStringId())).withRel("finish"));
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .cancel((Authentication) null, task.getStringId())).withRel("cancel"));
        // TODO: NAE-1969 fix
//        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
//                .getData(task.getStringId(), null, null)).withRel("data"));
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .setData((Authentication) null, task.getStringId(), null)).withRel("data-edit"));
        try {
            add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                    .getFile(task.getStringId(), "")).withRel("file"));
        } catch (FileNotFoundException e) {
            log.error("Building links failed: ", e);
        }
    }
}
