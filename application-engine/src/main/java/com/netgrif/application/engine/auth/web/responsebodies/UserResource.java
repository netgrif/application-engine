package com.netgrif.application.engine.auth.web.responsebodies;


import com.netgrif.application.engine.objects.dto.response.user.UserDto;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;

public class UserResource extends EntityModel<UserDto> {

    public UserResource(UserDto content, String selfRel) {
        super(content, new ArrayList<>());
    }


}
