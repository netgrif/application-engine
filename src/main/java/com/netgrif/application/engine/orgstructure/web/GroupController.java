package com.netgrif.application.engine.orgstructure.web;

import com.netgrif.auth.service.GroupService;
import com.netgrif.application.engine.orgstructure.web.responsebodies.Group;
import com.netgrif.application.engine.orgstructure.web.responsebodies.GroupsResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/group")
@ConditionalOnProperty(
        value = "nae.group.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Group")
public class GroupController {

    private final GroupService service;

    public GroupController(GroupService service) {
        this.service = service;
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Get all groups in the system",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/all", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public GroupsResource getAllGroups() {
        Set<com.netgrif.core.auth.domain.Group> groups = service.findAll();
        Set<Group> groupResponse = groups.stream()
                .map(g -> new Group(g.getStringId(), g.getDisplayName()))
                .collect(Collectors.toCollection(HashSet::new));
        return new GroupsResource(groupResponse);
    }
}