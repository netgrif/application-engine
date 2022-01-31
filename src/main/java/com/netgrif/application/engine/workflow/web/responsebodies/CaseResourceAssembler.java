package com.netgrif.application.engine.workflow.web.responsebodies;


import com.netgrif.application.engine.workflow.domain.Case;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class CaseResourceAssembler implements RepresentationModelAssembler<Case, CaseResource> {
    @Override
    public CaseResource toModel(Case aCase) {
        return new CaseResource(aCase);
    }
}
