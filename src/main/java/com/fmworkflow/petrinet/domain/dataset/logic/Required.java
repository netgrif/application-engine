package com.fmworkflow.petrinet.domain.dataset.logic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
final public class Required implements IDataFunction {

    public Required() {
        super();
    }

    @Override
    public ObjectNode unsafeApply(ObjectNode jsonObject) throws Exception {
        return jsonObject.put("required", true);
    }
}