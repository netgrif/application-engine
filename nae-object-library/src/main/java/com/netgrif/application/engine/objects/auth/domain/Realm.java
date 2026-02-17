package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.auth.provider.AuthMethodConfig;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a security realm in the authentication system.
 * A realm is a security context that contains authentication methods, user IDs,
 * and security settings for a group of users.
 */
@Data
public abstract class Realm implements Serializable {

    /**
     * Serial version UID for serialization support.
     */
    @Serial
    private static final long serialVersionUID = -162168235241317688L;

    /**
     * Tenant identifier. Realm can belong to only one tenant.
     */
    private String tenantId;

    /**
     * Indicates whether this realm is the default realm per Tenant.
     * Only one realm can be set as default.
     */
    @NotNull
    private boolean defaultRealm = false;

    /**
     * The name of the realm. This field is required.
     */
    @NotNull
    private String name;

    /**
     * Optional description of the realm's purpose or characteristics.
     */
    private String description;

    /**
     * List of authentication methods configured for this realm.
     * Marked as transient to prevent serialization.
     */
    private transient List<AuthMethodConfig<?>> authMethods = new ArrayList<>();

    /**
     * Indicates whether this realm has administrative privileges.
     */
    private boolean adminRealm;

    /**
     * Determines if user blocking is enabled for this realm.
     * When true, users can be blocked after failed authentication attempts.
     */
    private boolean enableBlocking = true;

    /**
     * Maximum number of failed authentication attempts before blocking a user.
     * Default value is 10 attempts.
     */
    private int maxFailedAttempts = 10;

    /**
     * Duration in minutes for which a user remains blocked after exceeding
     * maximum failed attempts. Default value is 1 minute.
     */
    private int blockDurationMinutes = 1;

    /**
     * Indicates whether the realm allows public access.
     * When true, some resources may be accessible without authentication.
     */
    private boolean publicAccess = false;

    /**
     * Duration after which an authenticated session times out.
     * Default value is 30 minutes.
     */
    private Duration sessionTimeout = Duration.ofMinutes(30);

    /**
     * Duration after which a public session times out.
     * Default value is 2 hours.
     */
    private Duration publicSessionTimeout = Duration.ofHours(2);

    /**
     * If true, the realm has enabled limit of maximum allowed sessions
     * per user
     */
    private boolean enableLimitSessions = false;

    /**
     * Maximum allowed sessions per user. Attribute {@link #enableLimitSessions}
     * must be enabled.
     */
    @Positive
    private int maxSessionsAllowed = 1;

    /**
     * Constructs a new Realm instance with the specified name.
     *
     * @param name the name of the realm
     */
    public Realm(String name) {
        this.name = name;
    }

    /**
     * Adds an authentication method configuration to this realm.
     *
     * @param authMethodConfig the authentication method configuration to add
     */
    public void addAuthMethod(AuthMethodConfig<?> authMethodConfig) {
        authMethods.add(authMethodConfig);
    }

    /**
     * Removes an authentication method configuration from this realm.
     *
     * @param authMethodConfig the authentication method configuration to remove
     */
    public void removeAuthMethod(AuthMethodConfig<?> authMethodConfig) {
        authMethods.remove(authMethodConfig);
    }
}