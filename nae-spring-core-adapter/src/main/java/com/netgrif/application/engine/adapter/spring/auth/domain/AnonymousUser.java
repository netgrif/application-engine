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

@Data
public class AnonymousUser extends AbstractUser {

    private transient Duration sessionTimeout = Duration.ofHours(2);

    private final Map<String, Attribute<?>> attributes = new HashMap<>();

    public AnonymousUser(AnonymousUserRef ref, Authority anonymousAuthority) {
        this.id = new ObjectId();
        this.realmId = ref.getRealmId();
        this.username = "anonymous@" + this.realmId;
        this.firstName = ref.getDisplayName();
        this.lastName = "";

        this.authoritySet = new HashSet<>();
        if (ref.getAuthorities() != null && !ref.getAuthorities().isEmpty()) {
            this.authoritySet.addAll(ref.getAuthorities());
        } else {
            this.authoritySet.add(anonymousAuthority);
        }

        this.processRoles = ref.getProcessRoles() != null ? new HashSet<>(ref.getProcessRoles()) : new HashSet<>();
        this.groupIds = ref.getGroupIds() != null ? new HashSet<>(ref.getGroupIds()) : new HashSet<>();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
    }

    @Override
    public String getRealmId() {
        return realmId;
    }

    @Override
    public void setRealmId(String realmId) {
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getName() {
        return firstName;
    }

    @Override
    public String getAvatar() {
        return null;
    }

    @Override
    public void setAvatar(String avatar) {
    }

    @Override
    public void addGroupId(String groupId) {
        this.groupIds.add(groupId);
    }

    @Override
    public boolean validateRequiredAttributes() {
        return attributes.values().stream().noneMatch(attr -> attr.isRequired() && attr.getValue() == null);
    }

    @Override
    public void setAttribute(String key, Object value, boolean required) {
        attributes.put(key, new Attribute<>(value, required));
    }

    @Override
    public Object getAttributeValue(String key) {
        Attribute<?> a = attributes.get(key);
        return a != null ? a.getValue() : null;
    }

    @Override
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public boolean isAttributeSet(String key) {
        return attributes.containsKey(key) && attributes.get(key).getValue() != null;
    }

    @Override
    public Attribute<?> getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public String toString() {
        return "[AnonymousUser id=%s username=%s realm=%s]".formatted(id, username, realmId);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public void setPassword(String password) {
    }
}
