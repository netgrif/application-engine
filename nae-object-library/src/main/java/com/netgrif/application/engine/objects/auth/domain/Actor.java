package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;

import java.io.Serializable;
import java.util.Set;

public interface Actor extends Serializable {

    /**
     * Gets the {@link org.bson.types.ObjectId} of this authorizing object in string form
     *
     * @return the ID in string form
     * */
    String getStringId();

    /**
     * Gets id of {@link Realm}, where this {@link Actor} belongs to
     * @return the ID of {@link Realm} in string form
     * */
    String getRealmId();

    /**
     * Gets set of {@link Authority} objects that this authorizing object has assigned
     *
     * @return the set of authorities
     * */
    Set<Authority> getAuthorities();

    /**
     * Sets {@link Authority} objects to this authorizing object
     *
     * @param authorities set of {@link Authority}
     * */
    void setAuthorities(Set<Authority> authorities);

    /**
     * Gets {@link ProcessRole} objects that are assigned to this authorizing object
     *
     * @return set of {@link ProcessRole}
     * */
    Set<ProcessRole> getProcessRoles();

    /**
     * Gets {@link ProcessRole} objects that are assigned to this authorizing object
     *
     * @return set of {@link ProcessRole}
     * */
    Set<ProcessRole> getNegativeProcessRoles();

    /**
     * Sets {@link ProcessRole} objects to this authorizing object
     *
     * @param processRoles set of {@link ProcessRole}
     * */
    void setNegativeProcessRoles(Set<ProcessRole> processRoles);

    /**
     * Sets {@link ProcessRole} objects to this authorizing object
     *
     * @param processRoles set of {@link ProcessRole}
     * */
    void setProcessRoles(Set<ProcessRole> processRoles);

    /**
     * Gets {@link Group} IDs that are assigned to this authorizing object as child groups
     *
     * @return set of {@link Group} IDs
     * */
    Set<String> getGroupIds();

    /**
     * Gets {@link Group} objects that are assigned to this authorizing object as child groups
     *
     * @return set of {@link Group}
     * */
    Set<Group> getGroups();

    /**
     * Sets {@link Group} Ids to this authorizing object
     *
     * @param groupIds set of {@link Group} IDs
     * */
    void setGroupIds(Set<String> groupIds);

    /**
     * Adds {@link Group} ID to this authorizing object
     *
     * @param groupId {@link Group} ID to be added
     * */
    void addGroupId(String groupId);

    /**
     * Removes {@link Group} ID to this authorizing object
     *
     * @param groupId {@link Group} ID to be removed
     * */
    void removeGroupId(String groupId);

    /**
     * Adds {@link Authority} object to this authorizing object
     *
     * @param authority {@link Authority} object to be added
     * */
    void addAuthority(Authority authority);

    /**
     * Removes {@link Authority} object to this authorizing object
     *
     * @param authority {@link Authority} object to be removed
     * */
    void removeAuthority(Authority authority);

    /**
     * Adds {@link ProcessRole} object to this authorizing object
     *
     * @param role {@link ProcessRole} object to be added
     * */
    void addProcessRole(ProcessRole role);

    /**
     * Adds {@link ProcessRole} object to this authorizing object
     *
     * @param role {@link ProcessRole} object to be added
     * */
    void removeProcessRole(ProcessRole role);

    /**
     * Adds {@link ProcessRole} object to this authorizing object
     *
     * @param role {@link ProcessRole} object to be added
     * */
    void addNegativeProcessRole(ProcessRole role);

    /**
     * Adds {@link ProcessRole} object to this authorizing object
     *
     * @param role {@link ProcessRole} object to be added
     * */
    void removeNegativeProcessRole(ProcessRole role);

    /**
     * Sets attribute to authorizing object
     *
     * @param key of attribute
     * @param value of attribute
     * @param required whether the attribute is required or not
     * */
    void setAttribute(String key, Object value, boolean required);

    /**
     * Gets attribute value
     *
     * @param key of attribute
     * @return the attribute value
     * */
    Object getAttributeValue(String key);

    /**
     * Removes attribute
     *
     * @param key of attribute
     * */
    void removeAttribute(String key);

    /**
     * Checks, whether the attribute is set or not
     *
     * @param key of attribute
     * */
    boolean isAttributeSet(String key);

    /**
     * Gets attribute object
     *
     * @param key of attribute
     * @return the attribute object
     * */
    Attribute<?> getAttribute(String key);
}
