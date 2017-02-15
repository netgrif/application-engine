package com.fmworkflow.petrinet.domain.dataset.logic;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public abstract class LogicFunction implements ILogicFunction {
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
