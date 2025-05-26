package com.netgrif.application.engine.objects.auth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractUser extends AbstractActor {

    @NotNull
    protected String username;

    @NotNull
    protected String firstName;

    protected String middleName;

    @NotNull
    protected String lastName;

    protected String email;

    protected String avatar;
    
    public AbstractUser(ObjectId id, String realmId, String username, String firstName, String middleName, String lastName, String email, String avatar) {
        super(id, realmId);
        this.username = username;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.avatar = avatar;
    }

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
     * Retrieves the password associated with the user, encoded via a password encoder.
     *
     * @return the encoded password as a String, or null if no password is set
     */
    public abstract String getPassword();

    /**
     * Sets the password for the user. This method encodes the given password
     * and stores it securely in the user's credentials.
     *
     * @param password the password to be encoded and saved as a string
     */
    public abstract void setPassword(String password);

    /**
     * Sets a credential for the user.
     *
     * @param key        The unique identifier used to associate the credential.
     *                   This is typically used to distinguish between multiple credentials.
     * @param credential The credential object containing type, value, and other details 
     *                   such as order and enabled status, to be associated with the key.
     */
    public void setCredential(String key, Credential<?> credential) {}

    /**
     * Sets a credential with specified properties.
     *
     * @param type the type of the credential, such as "password" or "token"
     * @param value the value of the credential
     * @param order the priority order of the credential; lower values indicate higher priority
     * @param enabled indicates if the credential is enabled or disabled
     */
    public void setCredential(String type, String value, int order, boolean enabled) {}

    /**
     * Activates multi-factor authentication (MFA) for the specified type with the provided secret. 
     *
     * @param type the type of MFA to activate, e.g., "TOTP" or "SMS". It cannot be null or empty.
     * @param secret the shared secret used for the MFA method. It cannot be null or empty.
     */
    public void activateMFA(String type, String secret) {}

    /**
     * Activates Multi-Factor Authentication (MFA) for the specified type.
     *
     * @param type the type of MFA to be activated (e.g., "TOTP", "SMS").
     * @param secret the secret key used for the MFA setup.
     * @param enabled whether the MFA should be immediately enabled or not.
     * @throws IllegalArgumentException if the type or secret is null or empty.
     */
    public void activateMFA(String type, String secret, boolean enabled) {}

    /**
     * Checks if a credential of the specified type is enabled.
     *
     * @param type the type of the credential to check
     * @return true if the credential of the specified type is enabled, false otherwise
     */
    public boolean isCredentialEnabled(String type) {
        return false;
    }

    /**
     * Retrieves a credential associated with the specified type.
     *
     * @param type the type of the credential to retrieve
     * @return the credential of the given type, or null if not found
     */
    public Credential<?> getCredential(String type) {
        return null;
    }

    /**
     * Disables the credential associated with the specified type. If a credential with the provided type exists,
     * it will be marked as disabled.
     *
     * @param type the type of the credential to be disabled
     */
    public void disableCredential(String type) {}

    /**
     * Sets a property for a specific credential type.
     *
     * @param type the type of the credential to modify
     * @param key the property key to set
     * @param value the property value to assign; must be serializable
     */
    public void setCredentialProperty(String type, String key, Object value) {}

    /**
     * Retrieves a credential property based on the specified type and key.
     *
     * @param type the type of credential to retrieve the property from
     * @param key the key of the property to be retrieved
     * @return the value of the property as an Object, or null if the credential or property does not exist
     */
    public Object getCredentialProperty(String type, String key) {
        return null;
    }

    @Override
    public String getName() {
        return String.join(" ", firstName, lastName).trim();
    }

    @Override
    public String getFullName() {
        return String.join(" ", firstName,
                middleName != null && !middleName.isEmpty() ? middleName : "",
                lastName).trim();
    }
}
