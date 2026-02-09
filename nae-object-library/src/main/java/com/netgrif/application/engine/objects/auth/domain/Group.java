package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.annotations.EnsureCollection;
import com.netgrif.application.engine.objects.annotations.Indexed;
import com.querydsl.core.annotations.QueryEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a group entity in the authentication system.
 * Groups can contain members and subgroups, providing hierarchical organization of users.
 */
@Getter
@QueryEntity
@EnsureCollection
public class Group extends AbstractActor implements Serializable {

    /**
     * The unique identifier of the group.
     */
    @Setter
    @Indexed
    private String identifier;

    /**
     * The display name of the group shown in the user interface.
     */
    @Setter
    @Indexed
    private String displayName;

    /**
     * The unique identifier of the group owner.
     */
    @Setter
    @Indexed
    private String ownerId;

    /**
     * The username of the group owner.
     */
    @Setter
    @Indexed
    private String ownerUsername;

    /**
     * The timestamp when the group was created.
     */
    @Setter
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * The timestamp when the group was last modified.
     */
    @Setter
    private LocalDateTime modifiedAt = LocalDateTime.now();

    /**
     * Collection of member IDs belonging to this group.
     */
    private Set<String> memberIds = new HashSet<>();

    /**
     * Collection of subgroup IDs belonging to this group.
     */
    private Set<String> subgroupIds = new HashSet<>();

    /**
     * Protected constructor for creating a new group with a generated ObjectId.
     */
    protected Group() {
        this.id = new ObjectId();
    }

    /**
     * Constructs a new group with a specified ObjectId.
     * @param id The ObjectId to assign to this group
     */
    public Group(ObjectId id) {
        this.id = id;
    }

    /**
     * Constructs a new group with specified identifier and realm ID.
     * @param identifier The unique identifier for the group
     * @param realmId The ID of the realm this group belongs to
     */
    public Group(String identifier, String realmId) {
        this();
        this.identifier = identifier;
        this.realmId = realmId;
    }

    /**
     * Returns the display name of the group.
     * @return The group's display name
     */
    @Override
    public String getName() {
        return this.displayName;
    }

    /**
     * Returns the full name of the group, which is the same as its display name.
     * @return The group's display name
     */
    @Override
    public String getFullName() {
        return this.displayName;
    }

    /**
     * Adds a member to the group by their user ID.
     * @param userId The ID of the user to add as a member
     */
    public void addMemberId(String userId) {
        if (this.memberIds == null) {
            this.memberIds = new HashSet<>();
        }
        this.memberIds.add(userId);
    }

    /**
     * Removes a member from the group by their user ID.
     * @param userId The ID of the user to remove from the group
     */
    public void removeMemberId(String userId) {
        if (this.memberIds == null) {
            this.memberIds = new HashSet<>();
        } else {
            this.memberIds.remove(userId);
        }
    }

    /**
     * Sets the collection of member IDs for this group.
     * @param memberIds The set of member IDs to assign, or null to create an empty set
     */
    public void setMemberIds(Set<String> memberIds) {
        this.memberIds = memberIds == null ? new HashSet<>() : new HashSet<>(memberIds);
    }

    /**
     * Adds a subgroup to this group by its group ID.
     * @param groupId The ID of the group to add as a subgroup
     */
    public void addSubGroupId(String groupId) {
        if (this.subgroupIds == null) {
            this.subgroupIds = new HashSet<>();
        }
        this.subgroupIds.add(groupId);
    }

    /**
     * Removes a subgroup from this group by its group ID.
     * @param groupId The ID of the group to remove from subgroups
     */
    public void removeSubgroupId(String groupId) {
        if (this.subgroupIds == null) {
            this.subgroupIds = new HashSet<>();
        } else {
            this.subgroupIds.remove(groupId);
        }
    }

    /**
     * Sets the collection of subgroup IDs for this group.
     * @param subgroupIds The set of subgroup IDs to assign, or null to create an empty set
     */
    public void setSubgroupIds(Set<String> subgroupIds) {
        this.subgroupIds = subgroupIds == null ? new HashSet<>() : new HashSet<>(subgroupIds);
    }
}