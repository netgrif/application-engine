package com.netgrif.workflow.orgstructure.web;

import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.orgstructure.service.IGroupService;
import com.netgrif.workflow.orgstructure.web.responsebodies.GroupsMinimalResource;
import com.netgrif.workflow.orgstructure.web.responsebodies.GroupsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    private IGroupService service;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public GroupsResource getAllGroups() {
        Set<Group> groups = service.findAll();
        return new GroupsMinimalResource(groups);
    }
}