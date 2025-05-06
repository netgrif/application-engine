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
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User extends AbstractUser implements Serializable {

    private boolean emailVerified;

    private String avatar;

    private UserState state;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime modifiedAt = LocalDateTime.now();

    private Map<String, Credential<?>> credentials = new HashMap<>();

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
