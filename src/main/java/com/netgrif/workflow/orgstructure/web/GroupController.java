package com.netgrif.workflow.orgstructure.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.orgstructure.service.IGroupService;
import com.netgrif.workflow.orgstructure.web.responsebodies.GroupsMinimalResource;
import com.netgrif.workflow.orgstructure.web.responsebodies.GroupsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.netgrif.workflow.auth.domain.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @GetMapping(value = "/my")
    public GroupsResource getGroupsOfUser(Authentication auth) {
        User loggedUser = ((LoggedUser) auth.getPrincipal()).transformToUser();
        List<Long> groupIds = loggedUser.getGroups().stream()
                .map(Group::getId)
                .collect(Collectors.toList());
        Set<Group> groups = service.findAllById(groupIds);
        return new GroupsMinimalResource(groups);
    }
}