package com.netgrif.application.engine.objects.auth.provider;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;
import java.util.Map;

@Data
public class RealmUpdate implements Serializable {

    @Serial
    private static final long serialVersionUID = 6152799520915390996L;

    private String name;
    private String description;
    private Boolean enabled;
    private Integer order;
    private String realmId;
    private Map<String, Object> configuration;
}
