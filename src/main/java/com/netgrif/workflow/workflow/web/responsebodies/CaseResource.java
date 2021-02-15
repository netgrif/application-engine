package com.netgrif.workflow.workflow.web.responsebodies;


import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.web.WorkflowController;
import com.netgrif.workflow.workflow.web.requestbodies.CreateCaseBody;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

public class CaseResource extends EntityModel<Case>{

    public CaseResource(Case content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks(){
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(WorkflowController.class).createCase(new CreateCaseBody(), null)).withRel("create"));
    }
}
