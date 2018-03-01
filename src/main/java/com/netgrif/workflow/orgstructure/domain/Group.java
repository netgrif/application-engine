package com.netgrif.workflow.orgstructure.domain;


import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
@Data
public class Group {

    public static final String MEMBER_OF = "MEMBER_OF";
    public static final String CHILD_OF = "CHILD_OF";

    @GraphId
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    // TODO: 3/1/18 validation: unique name on the same level path
    private String name;

    @Relationship(type = MEMBER_OF, direction = Relationship.INCOMING)
    private Set<Member> members;

    @Relationship(type = CHILD_OF)
    private Group parentGroup;

    @Relationship(type = CHILD_OF, direction = Relationship.INCOMING)
    private Set<Group> childGroups;

    public Group() {
        members = new HashSet<>();
        childGroups = new HashSet<>();
    }

    public Group(String name) {
        this();
        this.name = name;
    }
}