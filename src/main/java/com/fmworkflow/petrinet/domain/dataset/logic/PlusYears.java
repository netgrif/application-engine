package com.fmworkflow.petrinet.domain.dataset.logic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Document
public class PlusYears implements LogicFunction {
    private String ref;
    private Integer value;

    public PlusYears(@NotNull String ref, @NotNull Integer value) {
        super();
        this.ref = ref;
        this.value = value;
    }

    @Override
    public ObjectNode unsafeApply(ObjectNode jsonObject) throws Exception {
        ObjectNode json = jsonObject.putObject("plusYears");
        json.put("ref", ref);
        json.put("value", value);
        return jsonObject;
    }

    public String getReferredField() {
        return ref;
    }

    public Integer getValue() {
        return value;
    }
}