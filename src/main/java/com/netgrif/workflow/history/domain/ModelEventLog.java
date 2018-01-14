package com.netgrif.workflow.history.domain;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ModelEventLog extends EventLog implements IModelEventLog {

    private String model;

    public ModelEventLog() {
    }

    @Override
    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String getModel() {
        return model;
    }
}