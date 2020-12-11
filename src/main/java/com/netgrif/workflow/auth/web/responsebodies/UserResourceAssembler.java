package com.netgrif.workflow.auth.web.responsebodies;

import com.netgrif.workflow.auth.domain.User;
import org.springframework.hateoas.ResourceAssembler;

import java.util.Locale;

public class UserResourceAssembler implements ResourceAssembler<User, UserResource> {

    private Locale locale;
    private String selfRel;
    private boolean small;
    private IUserFactory userFactory;

    public UserResourceAssembler(Locale locale, boolean small, String selfRel, IUserFactory userFactory) {
        this.locale = locale;
        this.selfRel = selfRel;
        this.small = small;
        this.userFactory = userFactory;
    }

    @Override
    public UserResource toResource(User entity) {
        return new UserResource(userFactory.getUser(entity, locale, small), selfRel);
    }
}