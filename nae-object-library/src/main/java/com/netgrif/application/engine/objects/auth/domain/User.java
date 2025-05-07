package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.querydsl.core.annotations.QueryEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Slf4j
@QueryEntity
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User extends AbstractUser implements Serializable {

    private boolean emailVerified;

    private String avatar;

    private UserState state;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime modifiedAt = LocalDateTime.now();

    private Map<String, Credential<?>> credentials = new HashMap<>();

    @Setter
    private Set<String> authMethods;

    public User() {
        this.id = new ObjectId();
        this.attributes = new HashMap<>();
        this.credentials = new HashMap<>();
        this.authMethods = new HashSet<>();
    }

    public User(ObjectId id) {
        this();
        this.id = id;
    }

    @Override
    public String getFullName() {
        return String.join(" ", firstName, middleName != null ? middleName : "", lastName);
    }

    @Override
    public String getToken() {
        Credential<?> tokenCredential = this.credentials.get("token");
        return tokenCredential != null ? tokenCredential.getValue().toString() : null;
    }

    @Override
    public void setToken(String token) {
        TokenCredential tokenCredential = new TokenCredential(token, 1, true);
        this.credentials.put(tokenCredential.getType(), tokenCredential);
    }

    @Override
    public String getPassword() {
        Credential<?> passCred = this.credentials.get("password");
        if (passCred == null) {
            return null;
        }
        return String.valueOf(passCred.getValue());
    }

    @Override
    public void setPassword(String password) {
        PasswordCredential passwordCredential = new PasswordCredential(password, 0, true);
        this.credentials.put("password", passwordCredential);
    }

    @Override
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

    @Override
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

    @Override
    public boolean isMFAEnabled(String type) {
        Credential<?> credential = this.getCredential(type);
        return credential != null && credential.isEnabled();
    }

    @Override
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

    @Override
    public void enableMFA(String type, String value, int order) {
        this.setCredential(type, value, order, true);
    }

    @Override
    public void disableMFA(String type) {
        if (this.credentials.containsKey(type)) {
            this.credentials.get(type).setEnabled(false);
        }
    }

    @Override
    public void activateMFA(String type, String secret) {
        this.activateMFA(type, secret, true);
    }

    @Override
    public void activateMFA(String type, String secret, boolean activate) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("MFA type cannot be null or empty");
        }
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("MFA secret cannot be null or empty");
        }
        MFAStringCredential mfaCred = new MFAStringCredential(type, secret, 1, activate);
        this.credentials.put("MFA-" + type, mfaCred);
    }

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

    @Override
    public Object getCredentialProperty(String type, String key) {
        Credential<?> credential = this.credentials.get(type);
        return credential != null ? credential.getProperty(key) : null;
    }

    @Override
    public void removeCredential(String type) {
        this.credentials.remove(type);
    }

    @Override
    public boolean hasCredential(String type) {
        return this.credentials.containsKey(type);
    }

    @Override
    public Credential<?> getCredential(String type) {
        return this.credentials.get(type);
    }

    /**
     * Add an authentication method to the user.
     *
     * @param authMethod the authentication method to add.
     */
    public void addAuthMethod(String authMethod) {
        this.authMethods.add(authMethod);
    }

    /**
     * Check if the user supports a specific authentication method.
     *
     * @param authMethod the authentication method to check.
     * @return true if the user supports the authentication method, false otherwise.
     */
    public boolean supportsAuthMethod(String authMethod) {
        return this.authMethods.contains(authMethod);
    }

    @Override
    public boolean validateRequiredAttributes() {
        for (Map.Entry<String, Attribute<?>> entry : attributes.entrySet()) {
            if (entry.getValue().isRequired() && entry.getValue().getValue() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param credentials
     */
    public void setCredentials(Map<String, Credential<?>> credentials) {
        this.credentials = credentials == null ? new HashMap<>() : new HashMap<>(credentials);
    }

    /**
     * @param key
     * @param credential
     */
    public void setCredential(String key, Credential<?> credential) {
        if (this.credentials == null) {
            this.credentials = new HashMap<>();
        }
        this.credentials.put(key, credential);
    }

    /**
     * @param key
     * @return
     */
    public Object getCredentialValue(String key) {
        if (this.credentials == null) {
            return null;
        }
        Credential<?> credential = this.credentials.get(key);
        return credential != null ? credential.getValue() : null;
    }

    /**
     * @param key
     */
    public void removeCredential(String key) {
        if (this.credentials == null) {
            this.credentials = new HashMap<>();
        } else {
            this.credentials.remove(key);
        }
    }

    /**
     * @param key
     * @return
     */
    public boolean isCredentialSet(String key) {
        if (this.credentials == null) {
            return false;
        }
        Credential<?> credential = this.credentials.get(key);
        return credential != null && credential.getValue() != null;
    }

    /**
     * @param key
     * @return
     */
    public Credential<?> getCredentials(String key) {
        if (this.credentials == null) {
            return null;
        }
        return this.credentials.get(key);
    }

    /**
     * @return
     */
    public Set<String> getCredentialsKeys() {
        if (this.credentials == null) {
            return Set.of();
        }
        return this.credentials.keySet();
    }

    /**
     * @return
     */
    public boolean isEnabled() {
        return this.state.equals(UserState.ACTIVE);
    }
}
