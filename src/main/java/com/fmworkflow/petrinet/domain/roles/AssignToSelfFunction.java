package com.fmworkflow.petrinet.domain.roles;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AssignToSelfFunction extends RoleFunction {
    public AssignToSelfFunction(String roleId) {
        super(roleId);
    }

    @Override
    public ObjectNode unsafeApply(ObjectNode jsonObject) throws Exception {
        return jsonObject.put("assignToSelf", getRoleId().equals(jsonObject.get("roleId").asText()));
    }
}
