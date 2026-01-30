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

/**
 * Represents a logged-in user in the application, extending the {@link AbstractUser} class.
 * This class maintains session-specific information and authentication details for a user
 * who has successfully logged into the system.
 * 
 * @see AbstractUser
 * @see Serializable
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class LoggedUser extends AbstractUser implements Serializable {

    /**
     * The authentication provider origin from which the user was authenticated.
     */
    private String providerOrigin;

    /**
     * Set of Multi-Factor Authentication methods enabled for this user.
     */
    private Set<String> mfaMethods;

    /**
     * The duration after which the user's session will timeout.
     * This field is marked as transient to prevent serialization.
     * Default value is 30 minutes.
     */
    private transient Duration sessionTimeout = Duration.ofMinutes(30);


    /**
     * Constructs a new LoggedUser instance with all attributes.
     *
     * @param id The unique identifier as {@link ObjectId}
     * @param realmId The identifier of the security realm
     * @param username The user's username
     * @param firstName The user's first name
     * @param middleName The user's middle name
     * @param lastName The user's last name
     * @param email The user's email address
     * @param avatar The user's avatar URL
     * @param activeWorkspaceId The identifier of user's workspace
     * @param providerOrigin The authentication provider origin
     * @param mfaMethods The set of enabled MFA methods
     * @param sessionTimeout The duration of session timeout
     */
    public LoggedUser(ObjectId id, String realmId, String username, String firstName, String middleName, String lastName,
                      String email, String avatar, String activeWorkspaceId, String providerOrigin, Set<String> mfaMethods,
                      Duration sessionTimeout) {
        super(id, realmId, username, firstName, middleName, lastName, email, avatar);
        this.activeWorkspaceId = activeWorkspaceId;
        this.providerOrigin = providerOrigin;
        this.mfaMethods = mfaMethods;
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * Constructs a new LoggedUser instance with String id and all other attributes.
     *
     * @param id The unique identifier as String
     * @param realmId The identifier of the security realm
     * @param username The user's username
     * @param firstName The user's first name
     * @param middleName The user's middle name
     * @param lastName The user's last name
     * @param email The user's email address
     * @param avatar The user's avatar URL
     * @param activeWorkspaceId The identifier of user's workspace
     * @param providerOrigin The authentication provider origin
     * @param mfaMethods The set of enabled MFA methods
     * @param sessionTimeout The duration of session timeout
     */
    public LoggedUser(String id, String realmId, String username, String firstName, String middleName, String lastName, String email, String avatar, String activeWorkspaceId, String providerOrigin, Set<String> mfaMethods, Duration sessionTimeout) {
        super(id, realmId, username, firstName, middleName, lastName, email, avatar);
        this.activeWorkspaceId = activeWorkspaceId;
        this.providerOrigin = providerOrigin;
        this.mfaMethods = mfaMethods;
        this.sessionTimeout = sessionTimeout;
    }
}