package com.netgrif.application.engine.objects.dto.response.group;

import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.dto.response.authority.AuthorityDto;
import com.netgrif.application.engine.objects.dto.response.petrinet.ProcessRoleDto;

import java.io.Serializable;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public record GroupDto(String id,
                       String displayName,
                       String identifier,
                       String ownerUsername,
                       Set<AuthorityDto> authoritySet,
                       Set<ProcessRoleDto> processRoles,
                       Set<String> groupIds,
                       Set<String> subGroupIds) implements Serializable {

    public static GroupDto fromGroup(Group group, Locale locale) {
        return new GroupDto(group.getStringId(), group.getDisplayName(), group.getIdentifier(), group.getOwnerUsername(),
                group.getAuthoritySet().stream().map(AuthorityDto::fromAuthority).collect(Collectors.toSet()),
                group.getProcessRoles().stream().map(processRole -> new ProcessRoleDto(processRole, locale)).collect(Collectors.toSet()),
                group.getGroupIds(),
                group.getSubgroupIds()
        );
    }

}
