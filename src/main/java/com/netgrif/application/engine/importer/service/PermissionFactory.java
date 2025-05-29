package com.netgrif.application.engine.importer.service;


import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PermissionFactory {

    public Map<CasePermission, Boolean> buildProcessPermissions(com.netgrif.application.engine.importer.model.CasePermission modelPermissions) {
        Map<CasePermission, Boolean> permissions = new HashMap<>();
        addPermission(permissions, modelPermissions.isCreate(), CasePermission.CREATE);
        addPermission(permissions, modelPermissions.isDelete(), CasePermission.DELETE);
        addPermission(permissions, modelPermissions.isView(), CasePermission.VIEW);
        return permissions;
    }

    public Map<TaskPermission, Boolean> buildTaskPermissions(com.netgrif.application.engine.importer.model.TaskPermission modelPermissions) {
        Map<TaskPermission, Boolean> permissions = new HashMap<>();
        addPerform(permissions, modelPermissions);
        addPermission(permissions, modelPermissions.isView(), TaskPermission.VIEW);
        addPermission(permissions, modelPermissions.isAssign(), TaskPermission.ASSIGN);
        addPermission(permissions, modelPermissions.isCancel(), TaskPermission.CANCEL);
        addPermission(permissions, modelPermissions.isFinish(), TaskPermission.FINISH);
        addPermission(permissions, modelPermissions.isReassign(), TaskPermission.REASSIGN);
        addPermission(permissions, modelPermissions.isViewDisabled(), TaskPermission.VIEW_DISABLED);
        return permissions;
    }

    private void addPerform(Map<TaskPermission, Boolean> permissions, com.netgrif.application.engine.importer.model.TaskPermission roleLogic) {
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