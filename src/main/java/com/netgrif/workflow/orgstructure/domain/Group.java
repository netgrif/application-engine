package com.netgrif.workflow.orgstructure.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Group {

    public static final String MEMBER_OF = "MEMBER_OF";
    public static final String CHILD_OF = "CHILD_OF";

    @GraphId
    @Getter
    private Long id;

    @NotNull
    @Getter
    @Setter
    // TODO: 3/1/18 validation: unique name on the same level path
    private String name;

    @Relationship(type = MEMBER_OF, direction = Relationship.INCOMING)
    @Getter
    @Setter
    private Set<Member> members;

    @Relationship(type = CHILD_OF)
    @Getter
    @Setter
    private Group parentGroup;

    /**
     * Set of child groups. To add child use setParent(this) on child.
     */
    @Relationship(type = CHILD_OF, direction = Relationship.INCOMING)
    @Getter
    @Setter
    private Set<Group> childGroups;

    public Group() {
        members = new HashSet<>();
        childGroups = new HashSet<>();
    }

    public Group(Long id) {
        this();
        this.id = id;
    }

    public Group(String name) {
        this();
        this.name = name;
    }

    public void addMember(Member member) {
        members.add(member);
        member.getGroups().add(this);
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}