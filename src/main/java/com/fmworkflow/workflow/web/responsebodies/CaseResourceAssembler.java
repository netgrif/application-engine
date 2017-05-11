package com.fmworkflow.workflow.web.responsebodies;


import com.fmworkflow.workflow.domain.Case;
import org.springframework.hateoas.ResourceAssembler;

public class CaseResourceAssembler implements ResourceAssembler<Case, CaseResource> {
    @Override
    public CaseResource toResource(Case aCase) {
        return new CaseResource(aCase);
    }
}
