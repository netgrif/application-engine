package com.netgrif.application.engine.objects.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public interface IUser extends Actor {

    String getEmail();

    void setEmail(String email);

    String getRealmId();

    void setRealmId(String realmId);

    String getUsername();

    void setUsername(String username);

    String getFirstName();

    void setFirstName(String name);

    String getLastName();

    void setLastName(String surname);

    String getFullName();

    String getName();

    String getAvatar();

    void setAvatar(String avatar);

    String getTelNumber();

    void setTelNumber(String telNumber);

    UserState getState();

    void setState(UserState state);

    LoggedUser transformToLoggedUser();

    Author transformToAuthor();

    boolean isActive();

    boolean isImpersonating();

    LocalDateTime getCreatedAt();

    Map<String, Attribute<?>> getAttributes();

    boolean isEnabled();

    boolean isEmailVerified();

    @JsonIgnore
    IUser getSelfOrImpersonated();

    @JsonIgnore
    IUser getImpersonated();

    void setImpersonated(IUser user);

    void enableMFA(String type, String value, int order);

    void disableMFA(String type);

    Set<String> getEnabledMFAMethods();

    boolean isMFAEnabled(String type);

    void activateMFA(String type, String secret);

    void activateMFA(String type, String secret, boolean activate);

    /**
     * Validate all required attributes.
     *
     * @return true if all required attributes are set, false otherwise.
     */
    boolean validateRequiredAttributes();

    /**
     * Get the credential of a specific type (e.g., "password", "token").
     *
     * @param type the credential type.
     * @return the credential, or null if it doesn't exist.
     */
    Credential getCredential(String type);

    /**
     * Get a specific credential value by its type.
     *
     * @param type the credential type (e.g., "password", "token").
     * @return the credential value, or null if it doesn't exist.
     */
    <T> Object getCredentialValue(String type);

    /**
     * Add or update a credential.
     *
     * @param type  the type of the credential (e.g., "password", "token")
     * @param value the credential value
     * @param order the order or priority of the credential
     */
    void setCredential(String type, String value, int order, boolean enabled);

    void addCredential(Credential<?> credential);

    /**
     * Add or update a property of a specific credential (e.g., expiration date for a token).
     *
     * @param type  the type of the credential
     * @param key   the property key
     * @param value the property value
     */
    void setCredentialProperty(String type, String key, Object value);

    /**
     * Get a specific property of a credential.
     *
     * @param type the type of the credential
     * @param key  the property key
     * @return the value of the property, or null if the property doesn't exist
     */
    Object getCredentialProperty(String type, String key);

    /**
     * Remove a credential by its type.
     *
     * @param type the type of the credential (e.g., "password", "token")
     */
    void removeCredential(String type);

    /**
     * Check if a credential of a specific type exists.
     *
     * @param type the credential type
     * @return true if the credential exists, false otherwise
     */
    boolean hasCredential(String type);

}
