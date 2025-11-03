package com.netgrif.application.engine.workflow.web.responsebodies;


import com.netgrif.application.engine.objects.dto.response.workflow.CaseDto;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class CaseResourceAssembler implements RepresentationModelAssembler<CaseDto, CaseResource> {
    @Override
    public CaseResource toModel(CaseDto aCase) {
        return new CaseResource(aCase);
    }
}
