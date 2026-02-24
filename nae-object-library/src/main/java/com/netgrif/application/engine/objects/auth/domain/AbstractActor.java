package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.enums.WorkspacePermission;
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

/**
 * Abstract base class for actors in the system representing entities with authentication and authorization capabilities.
 * Implements Serializable for object persistence.
 */
@Getter
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractActor implements Serializable {

    /** Unique MongoDB identifier of the actor */
    protected ObjectId id;

    /** Identifier of the security realm this actor belongs to */
    @Setter
    protected String realmId;

    /** Map of actor's attributes with their values and requirement status */
    protected Map<String, Attribute<?>> attributes = new HashMap<>();

    /** Set of authorities (permissions) assigned to this actor */
    protected Set<Authority> authoritySet = new HashSet<>();

    /** Set of process-specific roles assigned to this actor */
    protected Set<ProcessRole> processRoles = new HashSet<>();

    /** Set of group identifiers this actor belongs to */
    protected Set<String> groupIds = new HashSet<>();

    /** todo javadoc workspaceId:permission */
    protected Map<String, WorkspacePermission> workspacePermissions = new HashMap<>();

    public AbstractActor(ObjectId id, String realmId) {
        this.id = id;
        this.realmId = realmId;
    }

    public AbstractActor(String stringId, String realmId) {
        this(new ObjectId(stringId), realmId);
    }

    /**
     * Returns the actor's name.
     * @return the name of the actor
     */
    abstract String getName();

    /**
     * Returns the actor's full name.
     * @return the full name of the actor
     */
    abstract String getFullName();

    /**
     * Converts the MongoDB ObjectId to its string representation.
     * @return string representation of the actor's ID
     */
    public String getStringId() {
        return this.id.toString();
    }

    /**
     * Sets the actor's ID from a string representation of MongoDB ObjectId.
     * @param id string representation of MongoDB ObjectId
     */
    public void setId(String id) {
        this.id = new ObjectId(id);
    }

    /**
     * Sets the actor's ID directly with MongoDB ObjectId.
     * @param id MongoDB ObjectId instance
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * Sets the attributes map with a defensive copy.
     * @param attributeSet map of attributes to set, null creates empty map
     */
    public void setAttributes(Map<String, Attribute<?>> attributeSet) {
        this.attributes = attributeSet == null ? new HashMap<>() : new HashMap<>(attributeSet);
    }

    /**
     * Sets a single attribute with given key, value and required status.
     * @param key attribute identifier
     * @param value attribute value
     * @param required whether the attribute is mandatory
     */
    public void setAttribute(String key, Object value, boolean required) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, new Attribute<>(value, required));
    }

    /**
     * Retrieves the value of an attribute by its key.
     * @param key attribute identifier
     * @return the value of the attribute or null if not found
     */
    public Object getAttributeValue(String key) {
        if (this.attributes == null) {
            return null;
        }
        Attribute<?> attr = this.attributes.get(key);
        return attr != null ? attr.getValue() : null;
    }

    /**
     * Removes an attribute by its key.
     * @param key attribute identifier to remove
     */
    public void removeAttribute(String key) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        } else {
            this.attributes.remove(key);
        }
    }

    /**
     * Checks if an attribute exists and has a non-null value.
     * @param key attribute identifier to check
     * @return true if attribute exists and has value, false otherwise
     */
    public boolean isAttributeSet(String key) {
        if (this.attributes == null) {
            return false;
        }
        Attribute<?> attr = this.attributes.get(key);
        return attr != null && attr.getValue() != null;
    }

    /**
     * Gets the attribute object by its key.
     * @param key attribute identifier
     * @return the Attribute object or null if not found
     */
    public Attribute<?> getAttribute(String key) {
        if (this.attributes == null) {
            return null;
        }
        return this.attributes.get(key);
    }

    /**
     * Returns all attribute keys.
     * @return set of attribute keys or empty set if no attributes
     */
    public Set<String> getAttributeKeys() {
        if (this.attributes == null) {
            return Set.of();
        }
        return this.attributes.keySet();
    }

    /**
     * Validates that all required attributes have non-null values.
     * @return true if all required attributes have values, false otherwise
     */
    public boolean validateRequiredAttributes() {
        for (Map.Entry<String, Attribute<?>> entry : attributes.entrySet()) {
            if (entry.getValue().isRequired() && entry.getValue().getValue() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the authority set with a defensive copy.
     * @param authoritySet set of authorities to set, null creates empty set
     */
    public void setAuthoritySet(Set<Authority> authoritySet) {
        this.authoritySet = authoritySet == null ? new HashSet<>() : new HashSet<>(authoritySet);
    }

    /**
     * Adds an authority to the actor.
     * @param authority the authority to add
     */
    public void addAuthority(Authority authority) {
        if (this.authoritySet == null) {
            this.authoritySet = new HashSet<>();
        }
        this.authoritySet.add(authority);
    }

    /**
     * Removes an authority from the actor.
     * @param authority the authority to remove
     */
    public void removeAuthority(Authority authority) {
        if (this.authoritySet == null) {
            this.authoritySet = new HashSet<>();
        } else if (!this.authoritySet.remove(authority)) {
            this.authoritySet.removeIf(it -> it.getName().equals(authority.getName()));
        }
    }

    /**
     * Removes an authority by its name.
     * @param name name of the authority to remove
     */
    public void removeAuthorityByName(String name) {
        if (this.authoritySet == null) {
            this.authoritySet = new HashSet<>();
        } else {
            this.authoritySet.removeIf(it -> it.getName().equals(name));
        }
    }

    /**
     * Sets the process roles with a defensive copy.
     * @param processRoleSet set of process roles to set, null creates empty set
     */
    public void setProcessRoles(Set<ProcessRole> processRoleSet) {
        this.processRoles = processRoleSet == null ? new HashSet<>() : new HashSet<>(processRoleSet);
    }

    /**
     * Adds a process role to the actor.
     * @param role the process role to add
     * @return true if the collection has been changed
     */
    public boolean addProcessRole(ProcessRole role) {
        if (this.processRoles == null) {
            this.processRoles = new HashSet<>();
        }
        return this.processRoles.add(role);
    }

    /**
     * Removes a process role from the actor.
     * @param role the process role to remove
     */
    public void removeProcessRole(ProcessRole role) {
        if (this.processRoles == null) {
            this.processRoles = new HashSet<>();
        } else if (!this.processRoles.remove(role)) {
            this.processRoles.removeIf(it -> it.getStringId().equals(role.getStringId()));
        }
    }

    /**
     * Removes a process role by its ID.
     * @param id ID of the process role to remove
     */
    public void removeProcessRoleById(String id) {
        if (this.processRoles == null) {
            this.processRoles = new HashSet<>();
        } else {
            this.processRoles.removeIf(it -> it.getStringId().equals(id));
        }
    }

    /**
     * Sets the group IDs with a defensive copy.
     * @param groupIds set of group IDs to set, null creates empty set
     */
    public void setGroupIds(Set<String> groupIds) {
        this.groupIds = groupIds == null ? new HashSet<>() : new HashSet<>(groupIds);
    }

    /**
     * Adds a group ID to the actor.
     * @param groupId the group ID to add
     */
    public void addGroupId(String groupId) {
        if (this.groupIds == null) {
            this.groupIds = new HashSet<>();
        }
        this.groupIds.add(groupId);
    }

    /**
     * Removes a group ID from the actor.
     * @param groupId the group ID to remove
     */
    public void removeGroupId(String groupId) {
        if (this.groupIds == null) {
            this.groupIds = new HashSet<>();
        } else {
            this.groupIds.remove(groupId);
        }
    }

    /**
     * todo javadoc
     */
    public void addWorkspacePermission(String workspaceId, WorkspacePermission permission) {
        if (this.workspacePermissions == null) {
            this.workspacePermissions = new HashMap<>();
        }
        if (workspaceId == null || permission == null) {
            return;
        }
        this.workspacePermissions.put(workspaceId, permission);
    }

    /**
     * todo javadoc
     */
    public void removeWorkspacePermission(String workspaceId) {
        if (this.workspacePermissions == null) {
            this.workspacePermissions = new HashMap<>();
        }
        if (workspaceId == null) {
            return;
        }
        this.workspacePermissions.remove(workspaceId);
    }

    // todo javadoc
    public void setWorkspacePermissions(Map<String, WorkspacePermission> workspacePermissions) {
        this.workspacePermissions = workspacePermissions == null ? new HashMap<>() : new HashMap<>(workspacePermissions);
    }

    // todo javadoc
    public boolean hasAuthority(String authorityName) {
        return this.authoritySet.stream().anyMatch(userAuthority -> userAuthority.getName().equals(authorityName));
    }

    /**
     * Checks if the actor has admin authority.
     * @return true if the actor has admin authority, false otherwise
     */
    public boolean isAdmin() {
        return this.authoritySet.stream().anyMatch(it -> it.getName().equals(Authority.admin));
    }
}