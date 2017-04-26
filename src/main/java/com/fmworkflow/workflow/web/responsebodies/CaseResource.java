package com.fmworkflow.workflow.web.responsebodies;


import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.web.WorkflowController;
import com.fmworkflow.workflow.web.requestbodies.CreateCaseBody;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;

public class CaseResource extends Resource<Case>{

    public CaseResource(Case content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks(){
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(WorkflowController.class).createCase(new CreateCaseBody())).withRel("create"));
    }
}
