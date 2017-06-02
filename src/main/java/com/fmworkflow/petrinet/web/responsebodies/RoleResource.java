package com.fmworkflow.petrinet.web.responsebodies;


import com.fmworkflow.petrinet.domain.roles.ProcessRole;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;

public class RoleResource extends Resource<ProcessRole>{
    public RoleResource(ProcessRole content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks(){

    }
}
