package com.fmworkflow.petrinet.domain.dataset.logic;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Visible extends LogicFunction {

    public Visible() {
        super();
        this.name = Visible.class.getName();
    }

    @Override
    public JSONObject unsafeApply(JSONObject jsonObject) throws Exception {
        return jsonObject.put("visible", true);
    }
}
