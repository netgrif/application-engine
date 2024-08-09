package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.auth.domain.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.Locale;

public class UserResourceAssembler implements RepresentationModelAssembler<IUser, UserResource> {

    private String selfRel;

    private boolean initialized = false;

    public UserResourceAssembler() {
    }

    public void initialize(String selfRel) {
        this.selfRel = selfRel;
        this.initialized = true;
    }

    @Override
    public UserResource toModel(IUser entity) {
        if (!initialized) {
            throw new IllegalStateException("You must initialize the UserResourceAssembler before calling the toResource method! To initialize the assembler call the initialize method.");
        }

        return new UserResource(new User(entity), selfRel);
    }
}