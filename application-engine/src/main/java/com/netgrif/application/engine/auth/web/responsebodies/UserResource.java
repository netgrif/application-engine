package com.netgrif.application.engine.auth.web.responsebodies;


import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;

public class UserResource extends EntityModel<UserDto> {

    public UserResource(UserDto content) {
        super(content, new ArrayList<>());
    }


}
