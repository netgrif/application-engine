package com.netgrif.application.engine.workflow.web.responsebodies;


import com.netgrif.application.engine.objects.dto.response.workflow.CaseDto;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.Locale;

public class CaseResourceAssembler implements RepresentationModelAssembler<Case, CaseResource> {
    private Locale locale;

    public CaseResourceAssembler(Locale locale) {
        this.locale = locale;
    }

    @Override
    public CaseResource toModel(Case aCase) {
        return new CaseResource(CaseDto.fromCase(aCase, locale));
    }
}
