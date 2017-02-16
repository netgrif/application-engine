package com.fmworkflow.petrinet.domain.dataset.logic;

import org.codehaus.jettison.json.JSONObject;

import java.util.function.Function;

public interface ILogicFunction extends Function<JSONObject, JSONObject> {

    @Override
    default JSONObject apply(JSONObject jsonObject) {
        try {
            return unsafeApply(jsonObject);
        } catch (Exception e) {
            throw new LogicNotApplicableException(e);
        }
    }

    JSONObject unsafeApply(JSONObject jsonObject) throws Exception;
}
