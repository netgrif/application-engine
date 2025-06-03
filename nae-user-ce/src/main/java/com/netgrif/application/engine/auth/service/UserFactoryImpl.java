package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.petrinet.web.responsebodies.ProcessRole;
import com.netgrif.application.engine.auth.web.responsebodies.User;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.petrinet.domain.workspace.DefaultWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class UserFactoryImpl implements UserFactory {

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private ProcessRoleFactory processRoleFactory;

    @Autowired
    private DefaultWorkspaceService defaultWorkspaceService;

    @Override
    public User getUser(IUser user, Locale locale) {
        User result = getUser(user);

        List<String> defaultRolesId = new ArrayList<>();
        List<String> anonymousRolesId = new ArrayList<>();
        defaultWorkspaceService.getAllWorkspaces().forEach(workspace -> {
            defaultRolesId.add(processRoleService.defaultRole(workspace.getId()).getStringId());
            anonymousRolesId.add(processRoleService.anonymousRole(workspace.getId()).getStringId());
        });

        result.setProcessRoles(user.getProcessRoles().stream().map(processRole -> {
            if (defaultRolesId.contains(processRole.getStringId()) || anonymousRolesId.contains(processRole.getStringId())) {
                return new ProcessRole(processRole, locale);
            }
            return processRoleFactory.getProcessRole(processRole, locale);
        }).collect(Collectors.toSet()));

        return result;
    }

    protected User getUser(IUser user) {
        return User.createUser(user);
    }
}
