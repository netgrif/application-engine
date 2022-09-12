package com.netgrif.application.engine.orgstructure.web;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.ldap.domain.LdapGroup;
import com.netgrif.application.engine.ldap.domain.LdapGroupRef;
import com.netgrif.application.engine.ldap.service.interfaces.ILdapGroupRefService;
import com.netgrif.application.engine.orgstructure.web.requestbodies.LdapGroupRoleAssignRequestBody;
import com.netgrif.application.engine.orgstructure.web.requestbodies.LdapGroupSearchBody;
import com.netgrif.application.engine.orgstructure.web.responsebodies.LdapGroupResponseBody;
import com.netgrif.application.engine.orgstructure.web.responsebodies.LdapGroupsResource;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/ldap")
@ConditionalOnExpression("${nae.ldap.enabled:false}")
@Tag(name = "Ldap")
public class LdapController {

    @Autowired
    protected ILdapGroupRefService service;

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Get all ldap groups",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public LdapGroupsResource getAllLdapGroups(@RequestBody LdapGroupSearchBody body, Authentication auth) {
        List<LdapGroupRef> groups;
        if (body == null || body.getFulltext().equals("")) {
            groups = service.findAllGroups();
        } else {
            groups = service.searchGroups(body.getFulltext());
        }
        List<LdapGroup> groupRoles = service.getAllLdapGroupRoles();
        Set<LdapGroupResponseBody> ldapGroupResponse = groups.stream()
                .map(group -> {
                    Set<ProcessRole> processRoleSet = groupRoles.stream().filter(ldapGroup -> ldapGroup.getDn().equals(group.getDn().toString())).map(LdapGroup::getProcessesRoles).flatMap(Collection::stream).collect(Collectors.toSet());
                    return new LdapGroupResponseBody(group.getDn().toString(), group.getCn(), group.getDescription(), processRoleSet);
                })
                .collect(Collectors.toCollection(HashSet::new));
        return new LdapGroupsResource(ldapGroupResponse);
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Assign role to the ldap group",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/role/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource assignRolesToLdapGroup(@RequestBody LdapGroupRoleAssignRequestBody requestBody, Authentication auth) {
        try {
            service.setRoleToLdapGroup(requestBody.getGroupDn(), requestBody.getRoleIds(), (LoggedUser) auth.getPrincipal());
            log.info("Process roles " + requestBody.getRoleIds() + " assigned to group " + requestBody.getGroupDn());
            return MessageResource.successMessage("Selected roles assigned to group " + requestBody.getGroupDn());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("Assigning roles to group " + requestBody.getGroupDn() + " has failed!");
        }
    }

}
