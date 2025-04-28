package com.netgrif.application.engine.auth.provider;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
public abstract class AbstractAuthConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = -1943793266822789260L;
    protected String id;
    protected String name;
    protected String authMethod;

    private boolean allowUserCreation = true;
    private boolean allowUserUpdate = true;

    public abstract AbstractAuthConfig of(Map<String, Object> map);
}
