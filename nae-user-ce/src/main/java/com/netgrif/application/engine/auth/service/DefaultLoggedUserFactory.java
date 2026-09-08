package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DefaultLoggedUserFactory implements ActorTransformer.LoggedUserFactory {

    private final GroupService groupService;

    private final ProcessRoleService processRoleService;

    private final AuthorityService authorityService;

    @Override
    public LoggedUser create() {
        return new LoggedUserImpl();
    }

    @Override
    public void resolveProcessRoles(AbstractActor user) {
        Set<String> processRoleIds = user.getProcessRoleIds();
        user.getGroupIds().forEach(groupId -> {
            resolveProcessRolesRecursively(groupService.findById(groupId), processRoleIds);
        });

        user.getProcessRoleIds().forEach(processRoleId -> {
            ProcessRole role = processRoleService.findById(processRoleId);
            if (role != null) {
                user.getProcessRoles().add(role);
            }
        });
    }

    @Override
    public void resolveProcessRolesRecursively(AbstractActor actor, Set<String> processRoleIds) {
        processRoleIds.addAll(actor.getProcessRoleIds());
        if (!actor.getGroupIds().isEmpty()) {
            actor.getGroupIds().forEach(groupId -> {
                Group group = groupService.findById(groupId);
                processRoleIds.addAll(group.getProcessRoleIds());
                resolveProcessRolesRecursively(group, processRoleIds);
            });
        }
    }

    @Override
    public void resolveAuthorities(AbstractActor user) {
        Set<String> authorityIds = user.getAuthorityIds();
        user.getGroupIds().forEach(groupId -> {
            resolveAuthoritiesRecursively(groupService.findById(groupId), authorityIds);
        });

        user.getAuthorityIds().forEach(authorityId -> {
            Authority authority = authorityService.getOne(authorityId);
            if (authority != null) {
                user.getAuthoritySet().add(authority);
            }
        });
    }

    @Override
    public void resolveAuthoritiesRecursively(AbstractActor actor, Set<String> authorityIds) {
        authorityIds.addAll(actor.getAuthorityIds());
        if (!actor.getGroupIds().isEmpty()) {
            actor.getGroupIds().forEach(groupId -> {
                Group group = groupService.findById(groupId);
                authorityIds.addAll(group.getAuthorityIds());
                resolveAuthoritiesRecursively(group, authorityIds);
            });
        }
    }
}
