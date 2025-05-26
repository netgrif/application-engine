package com.netgrif.application.engine.objects.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.Duration;
import java.util.Set;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class LoggedUser extends AbstractUser implements Serializable {

    private String workspaceId;
    private String providerOrigin;
    private Set<String> mfaMethods;
    private transient Duration sessionTimeout = Duration.ofMinutes(30);

    public LoggedUser(ObjectId id, String realmId, String username, String firstName, String middleName, String lastName, String email, String avatar, String workspaceId, String providerOrigin, Set<String> mfaMethods, Duration sessionTimeout) {
        super(id, realmId, username, firstName, middleName, lastName, email, avatar);
        this.workspaceId = workspaceId;
        this.providerOrigin = providerOrigin;
        this.mfaMethods = mfaMethods;
        this.sessionTimeout = sessionTimeout;
    }

    public LoggedUser(String id, String realmId, String username, String firstName, String middleName, String lastName, String email, String avatar, String workspaceId, String providerOrigin, Set<String> mfaMethods, Duration sessionTimeout) {
        super(id, realmId, username, firstName, middleName, lastName, email, avatar);
        this.workspaceId = workspaceId;
        this.providerOrigin = providerOrigin;
        this.mfaMethods = mfaMethods;
        this.sessionTimeout = sessionTimeout;
    }
}
