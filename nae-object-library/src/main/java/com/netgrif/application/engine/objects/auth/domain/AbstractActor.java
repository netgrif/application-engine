package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractActor implements Serializable {

    protected ObjectId id;

    @Setter
    protected String realmId;

    protected Map<String, Attribute<?>> attributes = new HashMap<>();
    protected Set<Authority> authoritySet = new HashSet<>();
    protected Set<ProcessRole> processRoles = new HashSet<>();
    protected Set<String> groupIds = new HashSet<>();

    /**
     * Gets the display name for a user or the group name for a group.
     *
     * @return the display name
     */
    abstract String getName();

    /**
     * Gets the {@link ObjectId} of this authorizing object in String form
     *
     * @return the ID in String form
     */
    public String getStringId() {
        return this.id.toString();
    }

    /**
     * Sets the ID of this authorizing object in String form
     *
     * @param id in String form
     */
    public void setId(String id) {
        this.id = new ObjectId(id);
    }

    /**
     * Sets the ID of this authorizing object in {@link ObjectId} form
     *
     * @param id in {@link ObjectId} form
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * Sets {@link Attribute} objects to this authorizing object
     *
     * @param attributeSet map of {@link Attribute}
     */
    public void setAttributes(Map<String, Attribute<?>> attributeSet) {
        this.attributes = attributeSet == null ? new HashMap<>() : new HashMap<>(attributeSet);
    }

    /**
     * Sets attribute to authorizing object
     *
     * @param key      of attribute
     * @param value    of attribute
     * @param required whether the attribute is required or not
     */
    public void setAttribute(String key, Object value, boolean required) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, new Attribute<>(value, required));
    }

    /**
     * Gets attribute value
     *
     * @param key of attribute
     * @return the attribute value
     */
    public Object getAttributeValue(String key) {
        if (this.attributes == null) {
            return null;
        }
        Attribute<?> attr = this.attributes.get(key);
        return attr != null ? attr.getValue() : null;
    }

    /**
     * Removes attribute
     *
     * @param key of attribute
     */
    public void removeAttribute(String key) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        } else {
            this.attributes.remove(key);
        }
    }

    /**
     * Checks, whether the attribute is set or not
     *
     * @param key of attribute
     */
    public boolean isAttributeSet(String key) {
        if (this.attributes == null) {
            return false;
        }
        Attribute<?> attr = this.attributes.get(key);
        return attr != null && attr.getValue() != null;
    }

    /**
     * Gets attribute object
     *
     * @param key of attribute
     * @return the attribute object
     */
    public Attribute<?> getAttribute(String key) {
        if (this.attributes == null) {
            return null;
        }
        return this.attributes.get(key);
    }

    /**
     * Gets all attribute keys.
     *
     * @return a set of attribute key strings
     */
    public Set<String> getAttributeKeys() {
        if (this.attributes == null) {
            return Set.of();
        }
        return this.attributes.keySet();
    }

    public boolean validateRequiredAttributes() {
        for (Map.Entry<String, Attribute<?>> entry : attributes.entrySet()) {
            if (entry.getValue().isRequired() && entry.getValue().getValue() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets {@link Authority} objects to this authorizing object
     *
     * @param authoritySet set of {@link Authority}
     */
    public void setAuthoritySet(Set<Authority> authoritySet) {
        this.authoritySet = authoritySet == null ? new HashSet<>() : new HashSet<>(authoritySet);
    }

    /**
     * Adds {@link Authority} object to this authorizing object
     *
     * @param authority {@link Authority} object to be added
     */
    public void addAuthority(Authority authority) {
        if (this.authoritySet == null) {
            this.authoritySet = new HashSet<>();
        }
        this.authoritySet.add(authority);
    }

    /**
     * Removes {@link Authority} object to this authorizing object
     *
     * @param authority {@link Authority} object to be removed
     */
    public void removeAuthority(Authority authority) {
        if (this.authoritySet == null) {
            this.authoritySet = new HashSet<>();
        } else if (!this.authoritySet.remove(authority)) {
            this.authoritySet.removeIf(it -> it.getName().equals(authority.getName()));
        }
    }

    /**
     * @param name
     */
    public void removeAuthorityByName(String name) {
        if (this.authoritySet == null) {
            this.authoritySet = new HashSet<>();
        } else {
            this.authoritySet.removeIf(it -> it.getName().equals(name));
        }
    }

    /**
     * Sets {@link ProcessRole} objects to this authorizing object
     *
     * @param processRoleSet set of {@link ProcessRole}
     */
    public void setProcessRoles(Set<ProcessRole> processRoleSet) {
        this.processRoles = processRoleSet == null ? new HashSet<>() : new HashSet<>(processRoleSet);
    }

    /**
     * Adds {@link ProcessRole} object to this authorizing object
     *
     * @param role {@link ProcessRole} object to be added
     */
    public void addProcessRole(ProcessRole role) {
        if (this.processRoles == null) {
            this.processRoles = new HashSet<>();
        }
        this.processRoles.add(role);
    }

    /**
     * Adds {@link ProcessRole} object to this authorizing object
     *
     * @param role {@link ProcessRole} object to be added
     */
    public void removeProcessRole(ProcessRole role) {
        if (this.processRoles == null) {
            this.processRoles = new HashSet<>();
        } else if (!this.processRoles.remove(role)) {
            this.processRoles.removeIf(it -> it.getStringId().equals(role.getStringId()));
        }
    }

    /**
     * @param id
     */
    public void removeProcessRoleById(String id) {
        if (this.processRoles == null) {
            this.processRoles = new HashSet<>();
        } else {
            this.processRoles.removeIf(it -> it.getStringId().equals(id));
        }
    }

    /**
     * Sets {@link Group} Ids to this authorizing object
     *
     * @param groupIds set of {@link Group} IDs
     */
    public void setGroupIds(Set<String> groupIds) {
        this.groupIds = groupIds == null ? new HashSet<>() : new HashSet<>(groupIds);
    }

    /**
     * Adds {@link Group} ID to this authorizing object
     *
     * @param groupId {@link Group} ID to be added
     */
    public void addGroupId(String groupId) {
        if (this.groupIds == null) {
            this.groupIds = new HashSet<>();
        }
        this.groupIds.add(groupId);
    }

    /**
     * Removes {@link Group} ID to this authorizing object
     *
     * @param groupId {@link Group} ID to be removed
     */
    public void removeGroupId(String groupId) {
        if (this.groupIds == null) {
            this.groupIds = new HashSet<>();
        } else {
            this.groupIds.remove(groupId);
        }
    }

    public boolean isAdmin() {
        return this.authoritySet.stream().anyMatch(it -> it.getName().equals(Authority.admin));
    }
}