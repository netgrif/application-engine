package com.netgrif.workflow.auth.web.responsebodies;

import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.auth.web.UserController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

public class OrganizationsResource extends Resources<Group> {

    public OrganizationsResource(Iterable<Group> content) {
        super(content, new Link[0]);
        buildLinks();
    }

    private void buildLinks(){
//        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
//                .getAllOrganizations()).withSelfRel());
    }
}
