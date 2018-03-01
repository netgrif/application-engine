package com.netgrif.workflow.orgstructure.web;

import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.orgstructure.service.IGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class GroupController {

    @Autowired
    private IGroupService service;

    public Set<Group> getAllGroups() {
        return service.findAll();
    }
}
