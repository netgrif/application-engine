package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.auth.domain.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.Locale;

public class UserResourceAssembler implements RepresentationModelAssembler<IUser, UserResource> {

    @Autowired
    private IUserFactory userFactory;

    private Locale locale;
    private String selfRel;
    private boolean small;

    private boolean initialized = false;

    public UserResourceAssembler() {
    }

    public void initialize(Locale locale, boolean small, String selfRel) {
        this.locale = locale;
        this.selfRel = selfRel;
        this.small = small;
        this.initialized = true;
    }

    @Override
    public UserResource toModel(IUser entity) {
        if (!initialized) {
            throw new IllegalStateException("You must initialize the UserResourceAssembler before calling the toResource method! To initialize the assembler call the initialize method.");
        }

        return new UserResource(small ? userFactory.getSmallUser(entity) : userFactory.getUser(entity, locale), selfRel);
    }
}