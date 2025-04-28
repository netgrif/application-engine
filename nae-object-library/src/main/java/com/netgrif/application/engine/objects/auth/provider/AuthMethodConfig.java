package com.netgrif.application.engine.objects.auth.provider;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AuthMethodConfig<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 6152799520915390903L;
    private String id;
    private String name;
    private String type;
    private String description;
    private boolean enabled;
    private T configuration;
    private String realmId;

    public AuthMethodConfig() {
        if (this.id == null) {
            this.id = new ObjectId().toString();
        }
    }
}
