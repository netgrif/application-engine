package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.workflow.web.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;


public class DataFieldsResource extends Resources<Field> {

    public DataFieldsResource(Iterable<Field> content, String taskId) {
        super(content, new ArrayList<Link>());
        buildLinks(taskId);
    }

    private void buildLinks(String taskId){
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
            .getData(taskId)).withSelfRel());
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
            .saveData(taskId, null)).withRel("edit"));
    }
}
