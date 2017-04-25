package com.fmworkflow.workflow.web.responsebodies;


import com.fmworkflow.workflow.web.WorkflowController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;

public class CasesResource extends Resources<CaseResource>{
    public CasesResource(Iterable<CaseResource> content, String method) {
        super(content, new ArrayList<>());
        buildLinks(method);
    }

    private void buildLinks(String method){
        if(method.equalsIgnoreCase("all"))
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                    .methodOn(WorkflowController.class).getAll()).withSelfRel());
        else
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                    .methodOn(WorkflowController.class).getAll()).withRel("all"));

        if(method.equalsIgnoreCase("search"))
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                    .methodOn(WorkflowController.class).searchCases(new ArrayList<>())).withSelfRel());
        else
            add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                    .methodOn(WorkflowController.class).searchCases(new ArrayList<>())).withRel("search"));
    }
}
