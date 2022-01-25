package com.netgrif.workflow.orgstructure.web.responsebodies;

import com.netgrif.workflow.orgstructure.web.GroupController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;


public class GroupsResource extends CollectionModel<Group> {

    public GroupsResource(Iterable<Group> content) {
        super(content);
        buildLinks();
    }

    private void buildLinks(){
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GroupController.class)
                .getAllGroups()).withSelfRel());
    }
}