package com.fmworkflow.petrinet.domain.dataset.logic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fmworkflow.petrinet.domain.dataset.Field;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class PlusYears implements LogicFunction {
    @DBRef
    private Field referredField;
    private Integer value;

    public PlusYears(Field referredField, Integer value) {
        super();
        this.referredField = referredField;
        this.value = value;
    }

    @Override
    public ObjectNode unsafeApply(ObjectNode jsonObject) throws Exception {
        ObjectNode json = jsonObject.putObject("plusYears");
        json.put("ref", referredField.getObjectId());
        json.put("value", value);
        return jsonObject;
    }

    public Field getReferredField() {
        return referredField;
    }

    public Integer getValue() {
        return value;
    }
}