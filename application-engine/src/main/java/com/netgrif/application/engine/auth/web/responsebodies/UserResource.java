package com.netgrif.application.engine.auth.web.responsebodies;


import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;

public class UserResource extends EntityModel<User> {

    public UserResource(User content, String selfRel) {
        super(content, new ArrayList<>());
    }


}
