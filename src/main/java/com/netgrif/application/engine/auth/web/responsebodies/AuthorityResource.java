package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.auth.domain.Authority;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;

public class AuthorityResource extends EntityModel<Authority> {

    public AuthorityResource(Authority content) {
        super(content, new ArrayList<>());
    }
}
