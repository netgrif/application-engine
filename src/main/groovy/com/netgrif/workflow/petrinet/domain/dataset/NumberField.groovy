package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
public class NumberField extends Field<Double> {

    public NumberField() {
        super();
    }
}