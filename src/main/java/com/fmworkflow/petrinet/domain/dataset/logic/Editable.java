package com.fmworkflow.petrinet.domain.dataset.logic;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
final public class Editable extends LogicFunction {

    public Editable() {
        super();
        this.name = "Editable";
    }

    @Override
    public JSONObject unsafeApply(JSONObject jsonObject) throws Exception {
        jsonObject.put("editable", true);
        return jsonObject;
    }
}