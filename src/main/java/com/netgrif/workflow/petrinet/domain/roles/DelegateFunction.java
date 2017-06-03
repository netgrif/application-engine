package com.netgrif.workflow.petrinet.domain.roles;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class DelegateFunction extends RoleFunction {
    public DelegateFunction(String roleId) {
        super(roleId);
    }

    @Override
    public ObjectNode unsafeApply(ObjectNode jsonObject) throws Exception {
        return jsonObject.put("delegate", jsonObject.get("roleIds").asText().contains(getRoleId()));
    }
}