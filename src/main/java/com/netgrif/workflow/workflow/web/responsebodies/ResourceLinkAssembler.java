package com.netgrif.workflow.workflow.web.responsebodies;


import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.web.TaskController;
import com.netgrif.workflow.workflow.web.WorkflowController;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

public class ResourceLinkAssembler {


    public static void addLinks(PagedResources pagedResources, Class type, String selfRel) {
        if (type.equals(Task.class)) addTasksLinks(pagedResources, selfRel);
        if (type.equals(Case.class)) addCasesLinks(pagedResources, selfRel);

    }

    private static void addTasksLinks(PagedResources pagedResources, String selfRel) {
        if (!selfRel.equalsIgnoreCase("all"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getAll(null, null, null, null)).withRel("all"));
        if (!selfRel.equalsIgnoreCase("case"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getAllByCases(null, null, null, null)).withRel("case"));
        if (!selfRel.equalsIgnoreCase("my"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getMy(null, null, null, null)).withRel("my"));
        if (!selfRel.equalsIgnoreCase("finished"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .getMyFinished(null, null, null, null)).withRel("finished"));
        if (!selfRel.equalsIgnoreCase("search"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .search(null, null, null, null, null)).withRel("search"));
        if (!selfRel.equalsIgnoreCase("count"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                    .count(null, null, null)).withRel("count"));
    }

    private static void addCasesLinks(PagedResources pagedResources, String selfRel) {
        if (!selfRel.equalsIgnoreCase("all"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                    .getAll(null, null)).withRel("all"));
        if (!selfRel.equalsIgnoreCase("search"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                    .search(null, null, null, null, null)).withRel("search"));
        if (!selfRel.equalsIgnoreCase("count"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                    .count(null, null)).withRel("count"));
        if (!selfRel.equalsIgnoreCase("author"))
            pagedResources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                    .findAllByAuthor(0L, "", null, null)).withRel("author"));
    }


}
