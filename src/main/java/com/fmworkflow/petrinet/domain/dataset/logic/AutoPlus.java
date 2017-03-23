package com.fmworkflow.petrinet.domain.dataset.logic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Document
public class AutoPlus implements IDataFunction {
    private String ref;
    private String value;

    public AutoPlus(@NotNull String ref, @NotNull String value) {
        super();
        this.ref = ref;
        this.value = value;
    }

    @Override
    public ObjectNode unsafeApply(ObjectNode jsonObject) throws Exception {
        ObjectNode json = jsonObject.putObject("autoPlus");
        json.put("ref", ref);
        json.put("value", value);
        return jsonObject;
    }

    public String getReferredField() {
        return ref;
    }

    public String getValue() {
        return value;
    }
}