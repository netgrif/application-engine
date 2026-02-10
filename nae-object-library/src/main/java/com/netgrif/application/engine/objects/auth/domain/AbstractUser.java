package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.workspace.Workspace;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;


/**
 * Abstract base class for user entities in the system.
 * Extends AbstractActor to inherit authentication and authorization capabilities.
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractUser extends AbstractActor {

    /**
     * todo javadoc The identifier of the workspace associated with the logged user.
     */
    // TODO: implement adapter class and mark this attribute as Transient
    protected String activeWorkspaceId;

    /**
     * Username used for authentication and identification
     */
    @NotNull
    protected String username;

    /**
     * User's first/given name
     */
    @NotNull
    protected String firstName;

    /**
     * User's middle name (optional)
     */
    protected String middleName;

    /**
     * User's last/family name
     */
    @NotNull
    protected String lastName;

    /**
     * User's email address
     */
    protected String email;

    /**
     * URL or identifier of the user's avatar image
     */
    protected String avatar;

    /**
     * Constructs a new user with Object ID.
     *
     * @param id         MongoDB ObjectId of the user
     * @param realmId    Security realm identifier
     * @param username   User's login name
     * @param firstName  User's first name
     * @param middleName User's middle name
     * @param lastName   User's last name
     * @param email      User's email address
     * @param avatar     User's avatar URL/identifier
     */
    public AbstractUser(ObjectId id, String realmId, String username, String firstName, String middleName, String lastName, String email, String avatar) {
        super(id, realmId);
        this.username = username;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.avatar = avatar;
    }

    /**
     * Constructs a new user with String ID.
     *
     * @param id         String representation of MongoDB ObjectId
     * @param realmId    Security realm identifier
     * @param username   User's login name
     * @param firstName  User's first name
     * @param middleName User's middle name
     * @param lastName   User's last name
     * @param email      User's email address
     * @param avatar     User's avatar URL/identifier
     */
    public AbstractUser(String id, String realmId, String username, String firstName, String middleName, String lastName, String email, String avatar) {
        super(id, realmId);
        this.username = username;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.avatar = avatar;
    }

    /**
     * Gets the user's password.
     *
     * @return the user's password
     */
    public abstract String getPassword();

    /**
     * Sets the user's password.
     *
     * @param password new password to set
     */
    public abstract void setPassword(String password);

    /**
     * Sets a credential for the user.
     *
     * @param key        credential identifier
     * @param credential credential value object
     */
    public void setCredential(String key, Credential<?> credential) {
    }

    /**
     * Sets a credential with specified parameters.
     *
     * @param type    credential type
     * @param value   credential value
     * @param order   credential priority order
     * @param enabled whether credential is enabled
     */
    public void setCredential(String type, String value, int order, boolean enabled) {
    }

    /**
     * Activates Multi-Factor Authentication for the user.
     *
     * @param type   MFA type identifier
     * @param secret MFA secret key
     */
    public void activateMFA(String type, String secret) {
    }

    /**
     * Activates Multi-Factor Authentication with the enabled state.
     *
     * @param type    MFA type identifier
     * @param secret  MFA secret key
     * @param enabled whether MFA should be enabled
     */
    public void activateMFA(String type, String secret, boolean enabled) {
    }

    /**
     * Checks if a credential is enabled.
     *
     * @param type credential type to check
     * @return true if the credential is enabled, false otherwise
     */
    public boolean isCredentialEnabled(String type) {
        return false;
    }

    /**
     * Gets a credential by its type.
     *
     * @param type credential type
     * @return credential object or null if not found
     */
    public Credential<?> getCredential(String type) {
        return null;
    }

    /**
     * Disables a credential by its type.
     *
     * @param type credential type to disable
     */
    public void disableCredential(String type) {
    }

    /**
     * Sets a property for a specific credential.
     *
     * @param type  credential type
     * @param key   property key
     * @param value property value
     */
    public void setCredentialProperty(String type, String key, Object value) {
    }

    /**
     * Gets a property value from a credential.
     *
     * @param type credential type
     * @param key  property key
     * @return property value or null if not found
     */
    public Object getCredentialProperty(String type, String key) {
        return null;
    }

    /**
     * Returns a user's first and last name concatenated.
     */
    @Override
    public String getName() {
        return String.join(" ", firstName, lastName).trim();
    }

    // todo javadoc
    public String getActiveWorkspaceId() {
        if (isAdmin()) {
            return "";
        } else {
            return this.activeWorkspaceId == null || this.activeWorkspaceId.isEmpty() ? Workspace.FORBIDDEN_ID : this.activeWorkspaceId;
        }
    }

    /**
     * Returns the user's full name including middle name if present.
     */
    @Override
    public String getFullName() {
        return String.join(" ", firstName,
                middleName != null && !middleName.isEmpty() ? middleName : "",
                lastName).trim();
    }
}
