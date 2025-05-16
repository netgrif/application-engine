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

@Data
@Slf4j
@QueryEntity
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User extends AbstractUser implements Serializable {

    private boolean emailVerified;

    private UserState state;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime modifiedAt = LocalDateTime.now();

    private Map<String, Credential<?>> credentials = new HashMap<>();

    public User() {
        this.id = new ObjectId();
        this.attributes = new HashMap<>();
        this.credentials = new HashMap<>();
    }

    public User(ObjectId id) {
        this();
        this.id = id;
    }

    public String getToken() {
        Credential<?> tokenCredential = this.credentials.get("token");
        return tokenCredential != null ? tokenCredential.getValue().toString() : null;
    }

    public void setToken(String token) {
        TokenCredential tokenCredential = new TokenCredential(token, 1, true);
        this.credentials.put(tokenCredential.getType(), tokenCredential);
    }

    /**
     * Returns password from credentials encoded via PasswordEncoder
     *
     * @return encoded password as string
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
     * Saves password encoded via PasswordEncoder into password credential
     *
     * @param password encoded password as string
     */
    @Override
    public void setPassword(String password) {
        PasswordCredential passwordCredential = new PasswordCredential(password, 0, true);
        this.credentials.put("password", passwordCredential);
    }

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
    public boolean isCredentialEnabled(String type) {
        Credential<?> credential = this.getCredential(type);
        return credential != null && credential.isEnabled();
    }

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
    public void disableCredential(String type) {
        if (this.credentials.containsKey(type)) {
            this.credentials.get(type).setEnabled(false);
        }
    }

    @Override
    public void activateMFA(String type, String secret) {
        this.activateMFA(type, secret, true);
    }

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

    public boolean hasCredential(String type) {
        return this.credentials.containsKey(type);
    }

    @Override
    public Credential<?> getCredential(String type) {
        return this.credentials.get(type);
    }

    public void setCredentials(Map<String, Credential<?>> credentials) {
        this.credentials = credentials == null ? new HashMap<>() : new HashMap<>(credentials);
    }

    @Override
    public void setCredential(String type, String value, int order, boolean enabled) {
        StringCredential credential = new StringCredential(type, value, order, enabled);
        this.setCredential(type, credential);
    }

    @Override
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
    public boolean isActive() {
        return this.state.equals(UserState.ACTIVE);
    }
}
