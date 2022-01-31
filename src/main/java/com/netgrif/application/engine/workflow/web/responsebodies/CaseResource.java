package com.netgrif.application.engine.workflow.web.responsebodies;


import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.web.WorkflowController;
import com.netgrif.application.engine.workflow.web.requestbodies.CreateCaseBody;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

public class CaseResource extends EntityModel<Case> {

    public CaseResource(Case content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(WorkflowController.class).createCase(new CreateCaseBody(), null, LocaleContextHolder.getLocale())).withRel("create"));
    }
}
