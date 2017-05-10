package com.fmworkflow.workflow.web.responsebodies;


import com.fmworkflow.workflow.domain.Task;
import com.fmworkflow.workflow.web.TaskController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

public class ResourceLinkAssembler {


    public static void addLinks(PagedResources pagedResources, Class type, String selfRel) {
        if (type.equals(Task.class)) addTasksLinks(pagedResources, selfRel);

    }

    private static void addTasksLinks(PagedResources pagedResources, String selfRel) {
        if (!selfRel.equalsIgnoreCase("all"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getAll(null, null, null)).withRel("all"));
        if (!selfRel.equalsIgnoreCase("case"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getAllByCases(null, null, null)).withRel("case"));
        if (!selfRel.equalsIgnoreCase("my"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getMy(null, null, null)).withRel("my"));
        if (!selfRel.equalsIgnoreCase("finished"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getMyFinished(null, null, null)).withRel("finished"));
        if (!selfRel.equalsIgnoreCase("search"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .search(null, null, null, null)).withRel("search"));
    }


}
