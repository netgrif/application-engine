package com.fmworkflow.petrinet.domain.roles;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AssignToOtherFunction extends RoleFunction {
    public AssignToOtherFunction(String roleId) {
        super(roleId);
    }

    @Override
    public ObjectNode unsafeApply(ObjectNode jsonObject) throws Exception {
        return jsonObject.put("assignToOther", getRoleId().equals(jsonObject.get("roleId").asText()));
    }
}
