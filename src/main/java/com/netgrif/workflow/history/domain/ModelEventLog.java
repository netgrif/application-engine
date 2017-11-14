package com.netgrif.workflow.history.domain;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.File;

@Document
public class ModelEventLog extends EventLog implements IModelEventLog {

    private File model;

    public ModelEventLog() {
    }

    @Override
    public void setModel(File model) {
        this.model = model;
    }

    @Override
    public File getModel() {
        return model;
    }
}