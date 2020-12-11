package com.netgrif.workflow.auth.web.responsebodies;

import org.springframework.hateoas.ResourceAssembler;

import java.util.Locale;

public class UserResourceAssembler implements ResourceAssembler<User, UserResource> {

    private Locale locale;
    private String selfRel;
    private boolean small;

    public UserResourceAssembler(Locale locale, boolean small, String selfRel) {
        this.locale = locale;
        this.selfRel = selfRel;
        this.small = small;
    }

    @Override
    public UserResource toResource(User entity) {
        return new UserResource(entity, selfRel);
    }
}