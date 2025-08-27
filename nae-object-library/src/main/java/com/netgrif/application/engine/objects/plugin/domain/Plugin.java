package com.netgrif.application.engine.objects.plugin.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Domain class that represent the given plugin
 *
 */
@Data
public class Plugin implements Serializable {

    @Serial
    private static final long serialVersionUID = -8675728293188640186L;

    private String identifier;

    private String name;

    private String version;

    private String description;

    private String url;

    private int restPort;

    private int grpcPort;

    private boolean active;

    /**
     * Map of {@link EntryPoint}, key of the map is equivalent to EntryPoint.getName
     *
     */
    private Map<String, EntryPoint> entryPoints;

    private Map<String, String> metadata;

    public Plugin() {
        entryPoints = new HashMap<>();
        metadata = new LinkedHashMap<>();
    }

    @Builder
    public Plugin(String identifier, String name, String version, String description, String url, int restPort, int grpcPort, Map<String, EntryPoint> entryPoints, boolean active, Map<String, String> metadata) {
        this.identifier = identifier;
        this.name = name;
        this.version = version;
        this.description = description;
        this.url = url;
        this.restPort = restPort;
        this.grpcPort = grpcPort;
        this.entryPoints = entryPoints;
        this.active = active;
        this.metadata = metadata;
    }
}
