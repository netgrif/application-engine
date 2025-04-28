package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.querydsl.core.annotations.QueryEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.util.*;

@QueryEntity
public class Group implements Actor {

    public static final String GROUP = "_group";

    @Getter
    private ObjectId id;

    @Getter
    @Setter
    private String realmId;

    @Getter
    @Setter
    private String identifier;

    @Setter
    @Getter
    private String ownerId;

    @Setter
    @Getter
    private String ownerUsername;

    @Setter
    @Getter
    private String displayName;

    @Getter
    @Setter
    private Set<String> memberIds;

    @Getter
    @Setter
    private Set<IUser> members;

    @Getter
    @Setter
    protected Set<Authority> authorities;

    @Getter
    @Setter
    protected Set<ProcessRole> processRoles;

    @Getter
    @Setter
    protected Set<String> groupIds;

    @Getter
    @Setter
    @BsonIgnore
    protected Set<Group> groups;

    private Map<String, Attribute<?>> attributes;

    protected Group() {
        id = new ObjectId();
        authorities = new HashSet<Authority>();
        processRoles = new HashSet<>();
        groupIds = new HashSet<>();
        groups = new HashSet<>();
        memberIds = new HashSet<>();
        members = new HashSet<>();
    }

    public Group(ObjectId id) {
        this();
        this.id = id;
    }

    public Group(String identifier, String realmId) {
        this();
        this.identifier = identifier;
        this.realmId = realmId;
    }

    @Override
    public String getStringId() {
        return id.toString();
    }

    @Override
    public void addGroupId(String groupId) {
        if (groupIds.stream().anyMatch(it -> it.equals(groupId))) {
            return;
        }
        groupIds.add(groupId);
    }

    @Override
    public void removeGroupId(String groupId) {
        groupIds.remove(groupId);
    }

    @Override
    public void addAuthority(Authority authority) {
        if (authorities.stream().anyMatch(it -> it.getStringId().equals(authority.getStringId()))) {
            return;
        }
        authorities.add(authority);
    }

    @Override
    public void removeAuthority(Authority authority) {
        authorities.remove(authority);
    }

    @Override
    public void addProcessRole(ProcessRole role) {
        if (processRoles.stream().anyMatch(it -> it.getStringId().equals(role.getStringId()))) {
            return;
        }
        processRoles.add(role);
    }

    @Override
    public void removeProcessRole(ProcessRole role) {
        processRoles.remove(role);
    }

    @Override
    public void setAttribute(String key, Object value, boolean required) {
        this.attributes.put(key, new Attribute<>(value, required));
    }

    @Override
    public Object getAttributeValue(String key) {
        Attribute<?> attribute = this.attributes.get(key);
        return attribute != null ? attribute.getValue() : null;
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

    public void addMemberId(String userId) {
        memberIds.add(userId);
    }

    public void removeMemberId(String userId) {
        memberIds.remove(userId);
    }
}
