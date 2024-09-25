package com.netgrif.application.engine.importer.service;


import com.netgrif.application.engine.importer.model.CaseLogic;
import com.netgrif.application.engine.importer.model.RoleRefLogic;
import com.netgrif.application.engine.petrinet.domain.roles.CasePermission;
import com.netgrif.application.engine.petrinet.domain.roles.TaskPermission;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PermissionFactory {

    public Map<CasePermission, Boolean> buildProcessPermissions(CaseLogic logic) {
        Map<CasePermission, Boolean> permissions = new HashMap<>();
        addPermission(permissions, logic.isCreate(), CasePermission.CREATE);
        addPermission(permissions, logic.isDelete(), CasePermission.DELETE);
        addPermission(permissions, logic.isView(), CasePermission.VIEW);
        return permissions;
    }

    public Map<TaskPermission, Boolean> buildTaskPermissions(RoleRefLogic roleLogic) {
        Map<TaskPermission, Boolean> permissions = new HashMap<>();
        addPerform(permissions, roleLogic);
        addPermission(permissions, roleLogic.isView(), TaskPermission.VIEW);
        addPermission(permissions, roleLogic.isAssign(), TaskPermission.ASSIGN);
        addPermission(permissions, roleLogic.isCancel(), TaskPermission.CANCEL);
        addPermission(permissions, roleLogic.isFinish(), TaskPermission.FINISH);
        addPermission(permissions, roleLogic.isReassign(), TaskPermission.REASSIGN);
        addPermission(permissions, roleLogic.isViewDisabled(), TaskPermission.VIEW_DISABLED);
        return permissions;
    }

    private void addPerform(Map<TaskPermission, Boolean> permissions, RoleRefLogic roleLogic) {
        if (roleLogic.isPerform() == null) {
            return;
        }
        for (TaskPermission permission : TaskPermission.values()) {
            permissions.put(permission, roleLogic.isPerform());
        }
    }

    private <T> void addPermission(Map<T, Boolean> permissions, Boolean permission, T rolePermission) {
        if (permission == null) {
            return;
        }
        permissions.put(rolePermission, permission);
    }
}