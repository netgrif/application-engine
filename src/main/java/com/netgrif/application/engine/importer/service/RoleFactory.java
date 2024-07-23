package com.netgrif.application.engine.importer.service;


import com.netgrif.application.engine.importer.model.RoleRefLogic;
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

    Map<RolePermission, Boolean> getPermissions(RoleRefLogic roleLogic) {
        Map<RolePermission, Boolean> permissions = new HashMap<>();

        addPerform(permissions, roleLogic);
        addPermission(permissions, roleLogic.isView(), RolePermission.VIEW);
        addPermission(permissions, roleLogic.isAssign(), RolePermission.ASSIGN);
        addPermission(permissions, roleLogic.isCancel(), RolePermission.CANCEL);
        addPermission(permissions, roleLogic.isFinish(), RolePermission.FINISH);
        addPermission(permissions, roleLogic.isReassign(), RolePermission.REASSIGN);
        addPermission(permissions, roleLogic.isViewDisabled(), RolePermission.VIEW_DISABLED);

        return permissions;
    }

//    Map<ProcessRolePermission, Boolean> getProcessPermissions(CaseLogic roleLogic) {
//        Map<ProcessRolePermission, Boolean> permissions = new HashMap<>();
//
//        addCreate(permissions, roleLogic);
//        addDelete(permissions, roleLogic);
//        addCaseView(permissions, roleLogic);
//
//        return permissions;
//    }

    private void addPerform(Map<RolePermission, Boolean> permissions, RoleRefLogic roleLogic) {
        if (roleLogic.isPerform() == null) {
            return;
        }
        permissions.put(RolePermission.VIEW, roleLogic.isPerform());
        permissions.put(RolePermission.ASSIGN, roleLogic.isPerform());
        permissions.put(RolePermission.CANCEL, roleLogic.isPerform());
        permissions.put(RolePermission.FINISH, roleLogic.isPerform());
        permissions.put(RolePermission.REASSIGN, roleLogic.isPerform());
        permissions.put(RolePermission.VIEW_DISABLED, roleLogic.isPerform());
    }

    private void addPermission(Map<RolePermission, Boolean> permissions, Boolean permission, RolePermission rolePermission) {
        if (permission == null) {
            return;
        }
        permissions.put(rolePermission, permission);
    }

//    private void addCreate(Map<ProcessRolePermission, Boolean> permissions, CaseLogic roleLogic) {
//        if (roleLogic.isCreate() != null) {
//            permissions.put(ProcessRolePermission.CREATE, roleLogic.isCreate());
//        }
//    }
//
//    private void addDelete(Map<ProcessRolePermission, Boolean> permissions, CaseLogic roleLogic) {
//        if (roleLogic.isDelete() != null) {
//            permissions.put(ProcessRolePermission.DELETE, roleLogic.isDelete());
//        }
//    }
//
//    private void addCaseView(Map<ProcessRolePermission, Boolean> permissions, CaseLogic roleLogic) {
//        if (roleLogic.isView() != null) {
//            permissions.put(ProcessRolePermission.VIEW, roleLogic.isView());
//        }
//    }
//
//    ProcessRole transitionRole(PetriNet net, Transition transition) {
//        ProcessRole role = new ProcessRole();
//        role.setName(transition.getImportId());
//        role.setImportId(net.getStringId() + "_" + transition.getImportId());
////        role.setDescription("Default role of transition "+transition.getTitle().getDefaultValue() + " in process "+net.getTitle().getDefaultValue());
//        role = repository.save(role);
//        return role;
//    }
}