package com.netgrif.workflow.orgstructure.web.responsebodies;

import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.orgstructure.web.GroupController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

public class GroupsResource extends Resources<Group> {

    public GroupsResource(Iterable<Group> content) {
        super(content);
        buildLinks();
    }

    private void buildLinks(){
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(GroupController.class)
                .getAllGroups()).withSelfRel());
    }
}