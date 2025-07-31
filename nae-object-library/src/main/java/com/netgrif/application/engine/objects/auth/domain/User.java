package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.utils.DateUtils;
import com.querydsl.core.annotations.QueryEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User entity class that extends AbstractUser and implements Serializable.
 * Represents a user in the system with authentication and authorization capabilities.
 */
@Data
@Slf4j
@QueryEntity
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User extends AbstractUser implements Serializable {

    /** Flag indicating whether the user's email has been verified */
    private boolean emailVerified;

    /** Current state of the user (e.g., ACTIVE, INACTIVE) */
    private UserState state;

    /** Timestamp when the user was created */
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Timestamp of the last modification to the user */
    private LocalDateTime modifiedAt = LocalDateTime.now();

    /** Map containing user's credentials with credential type as key */
    private Map<String, Credential<?>> credentials = new HashMap<>();

    /**
     * Default constructor initializing a new User with a generated ObjectId
     * and empty maps for attributes and credentials
     */
    public User() {
        this.id = new ObjectId();
        this.attributes = new HashMap<>();
        this.credentials = new HashMap<>();
    }

    /**
     * Constructor creating a User with a specified ObjectId
     * @param id The ObjectId to be assigned to the user
     */
    public User(ObjectId id) {
        this();
        this.id = id;
    }

    /**
     * Retrieves the user's authentication token from credentials
     * @return The token string if present, null otherwise
     */
    public String getToken() {
        Credential<?> tokenCredential = this.credentials.get("token");
        return tokenCredential != null ? tokenCredential.getValue().toString() : null;
    }

    /**
     * Sets the user's authentication token
     * @param token The token string to be stored
     */
    public void setToken(String token) {
        TokenCredential tokenCredential = new TokenCredential(token, 1, true);
        this.credentials.put(tokenCredential.getType(), tokenCredential);
    }

    /**
     * Retrieves the encoded password from credentials
     * @return The encoded password string if present, null otherwise
     */
    @Override
    public String getPassword() {
        Credential<?> passCred = this.credentials.get("password");
        if (passCred == null) {
            return null;
        }
        return String.valueOf(passCred.getValue());
    }

    /**
     * Sets the user's password (should be already encoded)
     * @param password The encoded password string to store
     */
    @Override
    public void setPassword(String password) {
        PasswordCredential passwordCredential = new PasswordCredential(password, 0, true);
        this.credentials.put("password", passwordCredential);
    }

    /**
     * Sets the expiration date for the user's token
     * @param expirationDate LocalDateTime when the token should expire
     */
    public void setExpirationDate(LocalDateTime expirationDate) {
        Credential<?> tokenCredential = this.credentials.get("token");
        if (tokenCredential != null) {
            tokenCredential.addProperty("expirationDate", expirationDate);
        } else {
            Credential<?> newTokenCredential = new TokenCredential("", 1, true);
            newTokenCredential.addProperty("expirationDate", expirationDate);
            this.credentials.put("token", newTokenCredential);
        }
    }

    /**
     * Retrieves the token expiration date
     * @return LocalDateTime of token expiration if present, null otherwise
     */
    public LocalDateTime getExpirationDate() {
        Credential<?> tokenCredential = this.credentials.get("token");
        if (tokenCredential != null) {
            Object obj = tokenCredential.getProperty("expirationDate");
            if (obj instanceof LocalDateTime) {
                return (LocalDateTime) obj;
            }
            return DateUtils.dateToLocalDateTime((Date) obj);
        }
        return null;
    }

    /**
     * Checks if a specific credential type is enabled
     * @param type The credential type to check
     * @return true if the credential exists and is enabled, false otherwise
     */
    @Override
    public boolean isCredentialEnabled(String type) {
        Credential<?> credential = this.getCredential(type);
        return credential != null && credential.isEnabled();
    }

    /**
     * Returns a set of enabled MFA method names
     * @return Set of strings representing enabled MFA methods
     */
    public Set<String> getEnabledMFAMethods() {
        return this.credentials.entrySet().stream()
                .filter(entry -> {
                    Credential<?> credential = entry.getValue();
                    return credential != null
                            && credential.getType() != null
                            && credential.getType().toUpperCase().startsWith("MFA")
                            && credential.isEnabled();
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Disables a specific credential type
     * @param type The credential type to disable
     */
    @Override
    public void disableCredential(String type) {
        if (this.credentials.containsKey(type)) {
            this.credentials.get(type).setEnabled(false);
        }
    }

    /**
     * Activates an MFA method with the given secret (enabled by default)
     * @param type The MFA type to activate
     * @param secret The secret key for the MFA method
     */
    @Override
    public void activateMFA(String type, String secret) {
        this.activateMFA(type, secret, true);
    }

    /**
     * Activates an MFA method with the given secret and enabled state
     * @param type The MFA type to activate
     * @param secret The secret key for the MFA method
     * @param enabled Whether the MFA method should be enabled
     * @throws IllegalArgumentException if type or secret is null or empty
     */
    @Override
    public void activateMFA(String type, String secret, boolean enabled) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("MFA type cannot be null or empty");
        }
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("MFA secret cannot be null or empty");
        }
        MFAStringCredential mfaCred = new MFAStringCredential(type, secret, 1, enabled);
        this.setCredential("MFA-" + type, mfaCred);
    }

    /**
     * Sets a property for a specific credential type
     * @param type The credential type
     * @param key The property key
     * @param value The property value (must be Serializable)
     */
    @Override
    public void setCredentialProperty(String type, String key, Object value) {
        Credential<?> credential = this.credentials.get(type);
        if (credential == null) {
            log.warn("Credential [{}] not found, cannot set property [{}]", type, key);
            return;
        }
        if (value instanceof Serializable serialVal) {
            credential.addProperty(key, serialVal);
            log.debug("Set credential property [{}:{}] for type [{}]", key, serialVal, type);
        } else {
            log.warn("Value for key [{}] is not serializable. Skipping.", key);
        }
    }

    /**
     * Retrieves a property value for a specific credential type
     * @param type The credential type
     * @param key The property key
     * @return The property value if found, null otherwise
     */
    @Override
    public Object getCredentialProperty(String type, String key) {
        Credential<?> credential = this.credentials.get(type);
        return credential != null ? credential.getProperty(key) : null;
    }

    /**
     * Checks if a specific credential type exists
     * @param type The credential type to check
     * @return true if the credential exists, false otherwise
     */
    public boolean hasCredential(String type) {
        return this.credentials.containsKey(type);
    }

    /**
     * Retrieves a credential by its type
     * @param type The credential type
     * @return The Credential object if found, null otherwise
     */
    @Override
    public Credential<?> getCredential(String type) {
        return this.credentials.get(type);
    }

    /**
     * Sets the entire credentials map
     * @param credentials The new credentials map (null creates empty map)
     */
    public void setCredentials(Map<String, Credential<?>> credentials) {
        this.credentials = credentials == null ? new HashMap<>() : new HashMap<>(credentials);
    }

    /**
     * Creates and sets a new string credential
     * @param type The credential type
     * @param value The credential value
     * @param order The credential order
     * @param enabled Whether the credential should be enabled
     */
    @Override
    public void setCredential(String type, String value, int order, boolean enabled) {
        StringCredential credential = new StringCredential(type, value, order, enabled);
        this.setCredential(type, credential);
    }

    /**
     * Sets a credential with the given key
     * @param key The credential key
     * @param credential The credential object to set
     */
    @Override
    public void setCredential(String key, Credential<?> credential) {
        if (this.credentials == null) {
            this.credentials = new HashMap<>();
        }
        this.credentials.put(key, credential);
    }

    /**
     * Gets the value of a credential by its key
     * @param key The credential key
     * @return The credential value if found, null otherwise
     */
    public Object getCredentialValue(String key) {
        if (this.credentials == null) {
            return null;
        }
        Credential<?> credential = this.credentials.get(key);
        return credential != null ? credential.getValue() : null;
    }

    /**
     * Removes a credential by its key
     * @param key The credential key to remove
     */
    public void removeCredential(String key) {
        if (this.credentials == null) {
            this.credentials = new HashMap<>();
        } else {
            this.credentials.remove(key);
        }
    }

    /**
     * Checks if a credential is set and has a value
     * @param key The credential key to check
     * @return true if credential exists and has a value, false otherwise
     */
    public boolean isCredentialSet(String key) {
        if (this.credentials == null) {
            return false;
        }
        Credential<?> credential = this.credentials.get(key);
        return credential != null && credential.getValue() != null;
    }

    /**
     * Gets all credential keys
     * @return Set of credential keys, empty set if no credentials exist
     */
    public Set<String> getCredentialsKeys() {
        if (this.credentials == null) {
            return Set.of();
        }
        return this.credentials.keySet();
    }

    /**
     * Checks if the user is in ACTIVE state
     * @return true if user state is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return this.state.equals(UserState.ACTIVE);
    }
}