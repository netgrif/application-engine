package com.netgrif.workflow.importer.service;


import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.importer.model.Logic;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class RoleFactory {

    @Autowired
    private ProcessRoleRepository repository;

    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository;

    Set<RolePermission> getPermissions(Logic roleLogic) {
        Set<RolePermission> permissions = new HashSet<>();

        addPerform(permissions, roleLogic);
        addDelegate(permissions, roleLogic);
        addCancel(permissions, roleLogic);

        return permissions;
    }

    private void addPerform(Set<RolePermission> permissions, Logic roleLogic) {
        if (roleLogic.isPerform() != null && roleLogic.isPerform())
            permissions.add(RolePermission.PERFORM);
    }

    private void addDelegate(Set<RolePermission> permissions, Logic roleLogic) {
        if (roleLogic.isDelegate() != null && roleLogic.isDelegate())
            permissions.add(RolePermission.DELEGATE);
    }

    private void addCancel(Set<RolePermission> permissions, Logic roleLogic) {
        if (roleLogic.isCancel() != null && roleLogic.isCancel())
            permissions.add(RolePermission.CANCEL);
    }

    ProcessRole transitionRole(PetriNet net, Transition transition) {
        ProcessRole role = new ProcessRole();
        role.setName(transition.getImportId());
        role.setImportId(net.getStringId() + "_" + transition.getImportId());
//        role.setDescription("Default role of transition "+transition.getTitle().getDefaultValue() + " in process "+net.getTitle().getDefaultValue());
        role = repository.save(role);
        userProcessRoleRepository.save(new UserProcessRole(role.getStringId()));
        return role;
    }
}