package com.netgrif.application.engine.workflow.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.netgrif.application.engine.objects.dto.response.task.TaskDto;
import com.netgrif.application.engine.workflow.web.TaskController;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.security.core.Authentication;

import java.io.FileNotFoundException;


@Getter
@Relation(collectionRelation = "tasks", itemRelation = "task")
public class LocalisedTaskResource extends RepresentationModel<LocalisedTaskResource> {

    public static final Logger log = LoggerFactory.getLogger(LocalisedTaskResource.class);

    @JsonUnwrapped
    private final TaskDto content;

    public LocalisedTaskResource(TaskDto content) {
        this.content = content;
        buildLinks();
    }

    private void buildLinks() {
        TaskDto taskDto = getContent();
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getOne(taskDto.stringId(), null)).withSelfRel());
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .assign((Authentication) null, taskDto.stringId(), null)).withRel("assign"));
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .delegate((Authentication) null, taskDto.stringId(), null, null)).withRel("delegate"));
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .finish((Authentication) null, taskDto.stringId(), null)).withRel("finish"));
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .cancel((Authentication) null, taskDto.stringId(), null)).withRel("cancel"));
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getData(taskDto.stringId(), null)).withRel("data"));
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .setData(taskDto.stringId(), null, null)).withRel("data-edit"));
        try {
            add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                    .getFile(taskDto.stringId(), "")).withRel("file"));
        } catch (FileNotFoundException e) {
            log.error("Building links failed: ", e);
        }
    }
}
