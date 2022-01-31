package com.netgrif.application.engine.workflow.web.responsebodies;


import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.web.TaskController;
import com.netgrif.application.engine.workflow.web.WorkflowController;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public class ResourceLinkAssembler {


    public static void addLinks(PagedModel pagedResources, Class type, String selfRel) {
        if (type.equals(Task.class)) addTasksLinks(pagedResources, selfRel);
        if (type.equals(Case.class)) addCasesLinks(pagedResources, selfRel);

    }

    private static void addTasksLinks(PagedModel pagedResources, String selfRel) {
        if (!selfRel.equalsIgnoreCase("all"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                    .getAll(null, null, null, null)).withRel("all"));
        if (!selfRel.equalsIgnoreCase("case"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                    .getAllByCases(null, null, null, null)).withRel("case"));
        if (!selfRel.equalsIgnoreCase("my"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                    .getMy(null, null, null, null)).withRel("my"));
        if (!selfRel.equalsIgnoreCase("finished"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                    .getMyFinished(null, null, null, null)).withRel("finished"));
        if (!selfRel.equalsIgnoreCase("search"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                    .search(null, null, null, null, null, null)).withRel("search"));
        if (!selfRel.equalsIgnoreCase("search_es"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                    .searchElastic(null, null, null, null, null, null)).withRel("search_es"));
        if (!selfRel.equalsIgnoreCase("count"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                    .count(null, null, null, null)).withRel("count"));
    }

    private static void addCasesLinks(PagedModel pagedResources, String selfRel) {
        if (!selfRel.equalsIgnoreCase("all"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                    .getAll(null, null)).withRel("all"));
        if (!selfRel.equalsIgnoreCase("search"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                    .search(null, MergeFilterOperation.OR, null, null, null, null)).withRel("search"));
        if (!selfRel.equalsIgnoreCase("count"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                    .count(null, MergeFilterOperation.OR, null, null)).withRel("count"));
        if (!selfRel.equalsIgnoreCase("author"))
            pagedResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                    .findAllByAuthor(null, "", null, null)).withRel("author"));
    }


}
