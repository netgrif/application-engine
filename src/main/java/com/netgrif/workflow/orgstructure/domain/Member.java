package com.netgrif.workflow.orgstructure.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
@Data
public class Member {

    @GraphId
    @Setter(AccessLevel.NONE)
    private Long id;

    private Long userId;

    private String name;

    private String surname;

    private String email;

    @Relationship(type = Group.MEMBER_OF)
    private Set<Group> groups;

    public Member() {
        groups = new HashSet<>();
    }

    public void addGroup(Group group) {
        groups.add(group);
    }
}