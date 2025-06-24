package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.utils.DateUtils;
import com.querydsl.core.annotations.QueryEntity;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Slf4j
@QueryEntity
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class User extends AbstractUser implements RegisteredUser, Serializable {

    private ObjectId id;

    @Setter
    private String realmId;

    @Setter
    private String username;

    @Setter
    private String email;

    @Setter
    private boolean emailVerified;

    @Setter
    @NotNull
    private String firstName;

    @Setter
    private String middleName;

    @Setter
    @NotNull
    private String lastName;

    @Setter
    private String avatar;

    @Setter
    private boolean enabled;

    @Setter
    private LocalDateTime createdAt = LocalDateTime.now();

    @Setter
    private Map<String, Attribute<?>> attributes;

    @Setter
    private Map<String, Credential<?>> credentials;

    @Setter
    private Set<String> authMethods;

    public User() {
        this.id = new ObjectId();
        this.attributes = new HashMap<>();
        this.credentials = new HashMap<>();
        this.authMethods = new HashSet<>();
        this.processRoles = new HashSet<>();
        this.negativeProcessRoles = new HashSet<>();
        this.authorities = new HashSet<>();
    }

    public User(ObjectId id) {
        this();
        this.id = id;
    }

    @Override
    public String getStringId() {
        return this.id.toString();
    }

    @Override
    public String getFullName() {
        return String.join(" ", firstName, middleName != null ? middleName : "", lastName);
    }

    @Override
    public String getName() {
        return String.join(" ", firstName, lastName);
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
    public Object getAttributeValue(String key) {
        if (attributes == null) {
            return null;
        }
        Attribute<?> attribute = this.attributes.get(key);
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public void setAttribute(String key, Object value, boolean required) {
        Attribute<?> attribute = new Attribute<>(value, required);
        this.attributes.put(key, attribute);
    }

    @Override
    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }

    @Override
    public boolean isAttributeSet(String key) {
        Attribute<?> attribute = this.attributes.get(key);
        return attribute != null && attribute.getValue() != null;
    }

    @Override
    public Attribute<?> getAttribute(String key) {
        return this.attributes.get(key);
    }

    @Override
    public void addGroupId(String groupId) {
        this.groupIds.add(groupId);
    }

    @Override
    public <T> Object getCredentialValue(String type) {
        Credential<?> credential = this.credentials.get(type);
        return credential != null ? credential.getValue() : null;
    }

    @Override
    public void addCredential(Credential<?> credential) {
        this.credentials.put(credential.type, credential);
    }


    @Override
    public void setCredential(String type, String value, int order, boolean enabled) {
        StringCredential credential = new StringCredential(type, value, order, enabled);
        this.credentials.put(type, credential);
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
    public void addAuthority(Authority authority) {
        this.authorities.add(authority);
    }

    @Override
    public void addProcessRole(ProcessRole role) {
        this.processRoles.add(role);
    }

    @Override
    public void removeProcessRole(ProcessRole role) {
        this.processRoles.remove(role);
    }

    @Override
    public void addNegativeProcessRole(ProcessRole role) {
        super.addNegativeProcessRole(role);
    }

    @Override
    public void removeNegativeProcessRole(ProcessRole role) {
        super.removeNegativeProcessRole(role);
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

    @Override
    public String getTelNumber() {
        if (attributes == null) {
            return null;
        }
        if (attributes.containsKey("tel")) {
            return (String) attributes.get("tel").getValue();
        }
        return null;
    }

    @Override
    public void setTelNumber(String telNumber) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        if (attributes.containsKey("tel")) {
            ((Attribute<String>) attributes.get("tel")).setValue(telNumber);
        }
        attributes.put("tel", new Attribute<>(telNumber, false));
    }

}
