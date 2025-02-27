package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.core.model.EntityModel;

public class UserResource extends EntityModel<User> {

    public UserResource(User content) {
        super(content);
//        buildLinks(selfRel);
    }
}
