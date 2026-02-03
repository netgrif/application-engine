package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Attribute;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Represents an anonymous user in the system, extending the {@link AbstractUser} class.
 * This class provides functionality for handling non-authenticated users with limited access rights.
 */
@Data
public class AnonymousUser extends AbstractUser {

    /**
     * The duration after which the anonymous user's session will timeout.
     * Default value is 2 hours.
     */
    private transient Duration sessionTimeout = Duration.ofHours(2);

    /**
     * Map storing user attributes with their values and required status.
     */
    private final Map<String, Attribute<?>> attributes = new HashMap<>();

    public AnonymousUser(AnonymousUserRef ref) {
        this.id = new ObjectId(ref.getId());
        this.realmId = ref.getRealmId();
        this.username = "anonymous@" + this.realmId;
        this.firstName = ref.getDisplayName();
        this.lastName = "";

        this.authoritySet = new HashSet<>();
        if (ref.getAuthorities() != null && !ref.getAuthorities().isEmpty()) {
            this.authoritySet.addAll(ref.getAuthorities());
        }

        this.processRoles = ref.getProcessRoles() != null ? new HashSet<>(ref.getProcessRoles()) : new HashSet<>();
        this.groupIds = ref.getGroupIds() != null ? new HashSet<>(ref.getGroupIds()) : new HashSet<>();
    }

    /**
     * Constructs a new anonymous user with specified reference and authority.
     *
     * @param ref the anonymous user reference containing basic user information
     * @param anonymousAuthority the authority to be assigned if no specific authorities are provided
     */
    public AnonymousUser(AnonymousUserRef ref, Authority anonymousAuthority) {
        this(ref);
        this.authoritySet = new HashSet<>();
        if (ref.getAuthorities() != null && !ref.getAuthorities().isEmpty()) {
            this.authoritySet.addAll(ref.getAuthorities());
        } else {
            this.authoritySet.add(anonymousAuthority);
        }
    }

    /**
     * {@inheritDoc}
     * @return the username of the anonymous user
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * {@inheritDoc}
     * Overridden to prevent modification of username for anonymous users.
     */
    @Override
    public void setUsername(String username) {
    }

    /**
     * {@inheritDoc}
     * @return the realm ID associated with this anonymous user
     */
    @Override
    public String getRealmId() {
        return realmId;
    }

    /**
     * {@inheritDoc}
     * Overridden to prevent modification of realm ID for anonymous users.
     */
    @Override
    public void setRealmId(String realmId) {
    }

    /**
     * {@inheritDoc}
     * @return the email address of the anonymous user
     */
    @Override
    public String getEmail() {
        return email;
    }

    /**
     * {@inheritDoc}
     * @param email the email address to set
     */
    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * {@inheritDoc}
     * @return the first name of the anonymous user
     */
    @Override
    public String getFirstName() {
        return firstName;
    }

    /**
     * {@inheritDoc}
     * @param firstName the first name to set
     */
    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * {@inheritDoc}
     * @return the last name of the anonymous user
     */
    @Override
    public String getLastName() {
        return lastName;
    }

    /**
     * {@inheritDoc}
     * @param lastName the last name to set
     */
    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * {@inheritDoc}
     * @return the first name as the display name for the anonymous user
     */
    @Override
    public String getName() {
        return firstName;
    }

    /**
     * {@inheritDoc}
     * @return null as anonymous users don't have avatars
     */
    @Override
    public String getAvatar() {
        return null;
    }

    /**
     * {@inheritDoc}
     * Overridden to prevent setting avatars for anonymous users.
     */
    @Override
    public void setAvatar(String avatar) {
    }

    /**
     * {@inheritDoc}
     * @param groupId the ID of the group to add
     */
    @Override
    public void addGroupId(String groupId) {
        this.groupIds.add(groupId);
    }

    /**
     * {@inheritDoc}
     * @return true if all required attributes have non-null values
     */
    @Override
    public boolean validateRequiredAttributes() {
        return attributes.values().stream().noneMatch(attr -> attr.isRequired() && attr.getValue() == null);
    }

    /**
     * {@inheritDoc}
     * @param key the attribute key
     * @param value the attribute value
     * @param required whether the attribute is required
     */
    @Override
    public void setAttribute(String key, Object value, boolean required) {
        attributes.put(key, new Attribute<>(value, required));
    }

    /**
     * {@inheritDoc}
     * @param key the key of the attribute
     * @return the value of the attribute, or null if not found
     */
    @Override
    public Object getAttributeValue(String key) {
        Attribute<?> a = attributes.get(key);
        return a != null ? a.getValue() : null;
    }

    /**
     * {@inheritDoc}
     * @param key the key of the attribute to remove
     */
    @Override
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    /**
     * {@inheritDoc}
     * @param key the key of the attribute to check
     * @return true if the attribute exists and has a non-null value
     */
    @Override
    public boolean isAttributeSet(String key) {
        return attributes.containsKey(key) && attributes.get(key).getValue() != null;
    }

    /**
     * {@inheritDoc}
     * @param key the key of the attribute to retrieve
     * @return the attribute object, or null if not found
     */
    @Override
    public Attribute<?> getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * {@inheritDoc}
     * @return a string representation of the anonymous user
     */
    @Override
    public String toString() {
        return "[AnonymousUser id=%s username=%s realm=%s]".formatted(id, username, realmId);
    }

    /**
     * {@inheritDoc}
     * @return null as anonymous users don't have passwords
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * {@inheritDoc}
     * Overridden to prevent setting passwords for anonymous users.
     */
    @Override
    public void setPassword(String password) {
    }
}