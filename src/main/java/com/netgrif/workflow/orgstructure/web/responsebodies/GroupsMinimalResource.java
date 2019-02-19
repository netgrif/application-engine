package com.netgrif.workflow.orgstructure.web.responsebodies;

import com.netgrif.workflow.orgstructure.domain.Group;

public class GroupsMinimalResource extends GroupsResource {

    public GroupsMinimalResource(Iterable<Group> content) {
        super(content);
        for (Group group : content) {
            group.setChildGroups(null);
            group.setMembers(null);
            group.setParentGroup(null);
        }
    }
}