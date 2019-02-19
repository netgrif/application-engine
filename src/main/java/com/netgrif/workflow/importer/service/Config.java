package com.netgrif.workflow.importer.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Config {

    private boolean notSaveObjects = true;

    public Config() {
    }

    public Config(boolean notSaveObjects) {
        this.notSaveObjects = notSaveObjects;
    }
}