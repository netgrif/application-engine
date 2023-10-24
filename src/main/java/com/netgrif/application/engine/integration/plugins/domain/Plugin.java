package com.netgrif.application.engine.integration.plugins.domain;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Document
public class Plugin {
    @Id
    private ObjectId _id;
    private String identifier;
    private String url;
    private long port;
    private Map<String, EntryPoint> entryPoints;

    public Plugin() {
        this.entryPoints = new LinkedHashMap<>();
    }
}
