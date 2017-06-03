package com.netgrif.workflow.petrinet.domain.roles;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AssignFunction extends RoleFunction {
    public AssignFunction(String roleId) {
        super(roleId);
    }

    @Override
    public ObjectNode unsafeApply(ObjectNode jsonObject) throws Exception {
        return jsonObject.put("assign", jsonObject.get("roleIds").asText().contains(getRoleId()));
    }
}