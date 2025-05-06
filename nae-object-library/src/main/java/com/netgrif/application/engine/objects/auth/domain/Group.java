package com.netgrif.application.engine.objects.auth.domain;

import com.querydsl.core.annotations.QueryEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@QueryEntity
public class Group extends AbstractActor implements Serializable {

    @Setter
    private String identifier;

    @Setter
    private String displayName;

    @Setter
    private String ownerId;

    @Setter
    private String ownerUsername;

    @Setter
    private LocalDateTime createdAt = LocalDateTime.now();

    @Setter
    private LocalDateTime modifiedAt = LocalDateTime.now();

    private Set<String> memberIds = new HashSet<>();

    private Set<String> subgroupIds = new HashSet<>();

    protected Group() {
        this.id = new ObjectId();
    }

    public Group(ObjectId id) {
        this.id = id;
    }

    public Group(String identifier, String realmId) {
        this();
        this.identifier = identifier;
        this.realmId = realmId;
    }

    @Override
    public String getName() {
        return this.displayName;
    }

    public void addMemberId(String userId) {
        if (this.memberIds == null) {
            this.memberIds = new HashSet<>();
        }
        this.memberIds.add(userId);
    }

    public void removeMemberId(String userId) {
        if (this.memberIds == null) {
            this.memberIds = new HashSet<>();
        } else {
            this.memberIds.remove(userId);
        }
    }

    public void setMemberIds(Set<String> memberIds) {
        this.memberIds = memberIds == null ? new HashSet<>() : new HashSet<>(memberIds);
    }

    public void addSubGroupId(String groupId) {
        if (this.subgroupIds == null) {
            this.subgroupIds = new HashSet<>();
        }
        this.subgroupIds.add(groupId);
    }

    public void removeSubgroupId(String groupId) {
        if (this.subgroupIds == null) {
            this.subgroupIds = new HashSet<>();
        } else {
            this.subgroupIds.remove(groupId);
        }
    }

    public void setSubgroupIds(Set<String> subgroupIds) {
        this.subgroupIds = subgroupIds == null ? new HashSet<>() : new HashSet<>(subgroupIds);
    }
}
