package com.netgrif.workflow.petrinet.web.responsebodies;


import org.springframework.hateoas.Resource;

import java.util.ArrayList;

public class ProcessRoleResource extends Resource<ProcessRole> {

    @Override
    public ProcessRole getContent() {
        return super.getContent();
    }

    public ProcessRoleResource(ProcessRole content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks() {
    }
}
