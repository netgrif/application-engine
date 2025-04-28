package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.auth.provider.AuthMethodConfig;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Duration;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Realm implements Serializable {

    @Serial
    private static final long serialVersionUID = -162168235241317688L;

    private String id;

    @NotNull
    private boolean defaultRealm = false;

    @NotNull
    private String name;

    private String description;

    private transient List<AuthMethodConfig<?>> authMethods = new ArrayList<>();

    private Set<String> userIds = new HashSet<>();

    private boolean adminRealm;

    private boolean enableBlocking = true;

    private int maxFailedAttempts = 10;

    private int blockDurationMinutes = 1;

    private boolean publicAccess = false;

    private Duration sessionTimeout = Duration.ofMinutes(30);

    private Duration publicSessionTimeout = Duration.ofHours(2);

    public Realm() {
    }

    public Realm(String name) {
        this.name = name;
    }

    public void addAuthMethod(AuthMethodConfig<?> authMethodConfig) {
        authMethods.add(authMethodConfig);
    }

    public void removeAuthMethod(AuthMethodConfig<?> authMethodConfig) {
        authMethods.remove(authMethodConfig);
    }
}
