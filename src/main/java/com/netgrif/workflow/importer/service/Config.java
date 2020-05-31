package com.netgrif.workflow.importer.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Config {

    private boolean saveObjects = true;

    public Config() {
    }

    public Config(boolean saveObjects) {
        this.saveObjects = saveObjects;
    }

    public static Config unsaved() {
        return Config.builder()
                .saveObjects(false)
                .build();

    }
}