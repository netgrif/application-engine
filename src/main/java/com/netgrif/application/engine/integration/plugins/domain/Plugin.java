package com.netgrif.application.engine.integration.plugins.domain;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Document
public class Plugin {
    @Id
    private ObjectId id;
    private String identifier;
    private String name;
    private String url;
    private long port;
    private boolean active;
    private Map<String, EntryPoint> entryPoints;

    public Plugin() {
        this.entryPoints = new LinkedHashMap<>();
    }
}
