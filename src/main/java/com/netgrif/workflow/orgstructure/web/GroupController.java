package com.netgrif.workflow.orgstructure.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.orgstructure.service.IGroupService;
import com.netgrif.workflow.orgstructure.web.responsebodies.GroupsMinimalResource;
import com.netgrif.workflow.orgstructure.web.responsebodies.GroupsResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.netgrif.workflow.auth.domain.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/group")
@ConditionalOnProperty(
        value = "nae.group.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Api(tags = {"Group"}, authorizations = @Authorization("BasicAuth"))
public class GroupController {

    @Autowired
    private IGroupService service;

    @ApiOperation(value = "Get all groups in the system", authorizations = @Authorization("BasicAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public GroupsResource getAllGroups() {
        Set<Group> groups = service.findAll();
        return new GroupsMinimalResource(groups);
    }

    @ApiOperation(value = "Get all the user's groups", authorizations = @Authorization("BasicAuth"))
    @GetMapping(value = "/my", produces = MediaTypes.HAL_JSON_VALUE)
    public GroupsResource getGroupsOfUser(Authentication auth) {
        User loggedUser = ((LoggedUser) auth.getPrincipal()).transformToUser();
        List<Long> groupIds = loggedUser.getGroups().stream()
                .map(Group::getId)
                .collect(Collectors.toList());
        Set<Group> groups = service.findAllById(groupIds);
        return new GroupsMinimalResource(groups);
    }
}