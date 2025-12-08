package com.netgrif.application.engine.workflow.web.responsebodies;


import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.netgrif.application.engine.objects.dto.response.workflow.CaseDto;
import com.netgrif.application.engine.workflow.web.WorkflowController;
import com.netgrif.application.engine.workflow.web.requestbodies.CreateCaseBody;
import lombok.Getter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

@Getter
@Relation(collectionRelation = "cases", itemRelation = "case")
public class CaseResource extends RepresentationModel<CaseResource> {

    @JsonUnwrapped
    private final CaseDto content;

    public CaseResource(CaseDto content) {
        this.content = content;
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(WorkflowController.class).createCase(new CreateCaseBody(), null, LocaleContextHolder.getLocale())).withRel("create"));
    }
}
