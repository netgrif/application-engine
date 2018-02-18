package com.netgrif.workflow.petrinet.web.responsebodies;


import org.springframework.hateoas.Resource;

import java.util.ArrayList;

public class LocalisedRoleResource extends Resource<LocalisedProcessRole>{
    public LocalisedRoleResource(LocalisedProcessRole content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks(){

    }
}
