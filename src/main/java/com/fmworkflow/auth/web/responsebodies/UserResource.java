package com.fmworkflow.auth.web.responsebodies;


import com.fmworkflow.auth.domain.User;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;

public class UserResource extends Resource<User>{
    public UserResource(User content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    public UserResource(User content, boolean small){
        this(content);
        if(small) {
            getContent().setTelNumber(null);
            getContent().setOrganizations(null);
            getContent().setRoles(null);
        }
    }

    private void buildLinks(){

    }
}
