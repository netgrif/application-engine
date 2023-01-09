package com.netgrif.application.engine.importer.service;


import com.netgrif.application.engine.importer.model.CaseLogic;
import com.netgrif.application.engine.importer.model.Logic;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RoleFactory {

    @Autowired
    private ProcessRoleRepository repository;

    Map<RolePermission, Boolean> getPermissions(Logic roleLogic) {
        Map<RolePermission, Boolean> permissions = new HashMap<>();

        addPerform(permissions, roleLogic);
        addFinish(permissions, roleLogic);
        addDelegate(permissions, roleLogic);
        addCancel(permissions, roleLogic);
        addAssign(permissions, roleLogic);
        addView(permissions, roleLogic);

        return permissions;
    }

    Map<ProcessRolePermission, Boolean> getProcessPermissions(CaseLogic roleLogic) {
        Map<ProcessRolePermission, Boolean> permissions = new HashMap<>();

        addCreate(permissions, roleLogic);
        addDelete(permissions, roleLogic);
        addCaseView(permissions, roleLogic);

        return permissions;
    }

    private void addPerform(Map<RolePermission, Boolean> permissions, Logic roleLogic) {
        if (roleLogic.isPerform() != null) {
            permissions.put(RolePermission.ASSIGN, roleLogic.isPerform());
            permissions.put(RolePermission.CANCEL, roleLogic.isPerform());
            permissions.put(RolePermission.FINISH, roleLogic.isPerform());
            permissions.put(RolePermission.VIEW, roleLogic.isPerform());
            permissions.put(RolePermission.SET, roleLogic.isPerform());
        }
    }

    private void addDelegate(Map<RolePermission, Boolean> permissions, Logic roleLogic) {
        if (roleLogic.isDelegate() != null) {
            permissions.put(RolePermission.DELEGATE, roleLogic.isDelegate());
        }
    }

    private void addCancel(Map<RolePermission, Boolean> permissions, Logic roleLogic) {
        if (roleLogic.isCancel() != null) {
            permissions.put(RolePermission.CANCEL, roleLogic.isCancel());
        }
    }

    private void addFinish(Map<RolePermission, Boolean> permissions, Logic roleLogic) {
        if (roleLogic.isFinish() != null) {
            permissions.put(RolePermission.FINISH, roleLogic.isFinish());
        }
    }

    private void addAssign(Map<RolePermission, Boolean> permissions, Logic roleLogic) {
        /* Part roleLogic.isAssigned() is deprecated and can be removed in future releases. */
        if (roleLogic.isAssigned() != null) {
            permissions.put(RolePermission.ASSIGN, roleLogic.isAssigned());
        } else if (roleLogic.isAssign() != null) {
            permissions.put(RolePermission.ASSIGN, roleLogic.isAssign());
        }
    }

    private void addCreate(Map<ProcessRolePermission, Boolean> permissions, CaseLogic roleLogic) {
        if (roleLogic.isCreate() != null) {
            permissions.put(ProcessRolePermission.CREATE, roleLogic.isCreate());
        }
    }

    private void addDelete(Map<ProcessRolePermission, Boolean> permissions, CaseLogic roleLogic) {
        if (roleLogic.isDelete() != null) {
            permissions.put(ProcessRolePermission.DELETE, roleLogic.isDelete());
        }
    }

    private void addView(Map<RolePermission, Boolean> permissions, Logic roleLogic) {
        if (roleLogic.isView() != null) {
            permissions.put(RolePermission.VIEW, roleLogic.isView());
        }
    }

    private void addCaseView(Map<ProcessRolePermission, Boolean> permissions, CaseLogic roleLogic) {
        if (roleLogic.isView() != null) {
            permissions.put(ProcessRolePermission.VIEW, roleLogic.isView());
        }
    }

    ProcessRole transitionRole(PetriNet net, Transition transition) {
        ProcessRole role = new ProcessRole();
        role.setName(transition.getImportId());
        role.setImportId(net.getStringId() + "_" + transition.getImportId());
//        role.setDescription("Default role of transition "+transition.getTitle().getDefaultValue() + " in process "+net.getTitle().getDefaultValue());
        role = repository.save(role);
        return role;
    }
}