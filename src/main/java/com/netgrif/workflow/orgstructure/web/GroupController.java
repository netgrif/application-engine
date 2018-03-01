package com.netgrif.workflow.orgstructure.web;

import com.netgrif.workflow.auth.web.responsebodies.GroupsResource;
import com.netgrif.workflow.orgstructure.service.IGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/res/group")
public class GroupController {

    @Autowired
    private IGroupService service;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public GroupsResource getAllGroups() {
        return new GroupsResource(service.findAll());
    }
}