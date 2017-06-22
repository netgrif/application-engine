package com.netgrif.workflow.petrinet.domain.roles;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.roles.LogicNotApplicableException;

import java.util.function.Function;

public interface IRoleFunction extends Function<ObjectNode, ObjectNode> {

    @Override
    default ObjectNode apply(ObjectNode jsonObject) {
        try {
            return unsafeApply(jsonObject);
        } catch (Exception e) {
            throw new LogicNotApplicableException(e);
        }
    }

    ObjectNode unsafeApply(ObjectNode jsonObject) throws Exception;
}