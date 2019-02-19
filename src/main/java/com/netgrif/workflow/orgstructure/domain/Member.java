package com.netgrif.workflow.orgstructure.domain;

import com.netgrif.workflow.auth.domain.User;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Member {

    @GraphId
    @Getter
    private Long id;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    @NotEmpty
    private String name;

    @Getter
    @Setter
    @NotEmpty
    private String surname;

    @Getter
    @Setter
    @NotEmpty
    private String email;

    /**
     * Set of groups this entity is member of. To add member to group use group.addMember()
     */
    @Relationship(type = Group.MEMBER_OF)
    @Getter
    @Setter
    private Set<Group> groups;

    public Member() {
        groups = new HashSet<>();
    }

    public Member(Long userId, String name, String surname, String email) {
        this();
        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public static Member from(User user) {
        return new Member(user.getId(), user.getName(), user.getSurname(), user.getEmail());
    }

    @Override
    public String toString() {
        return "Member{" +
                "email='" + email + '\'' +
                '}';
    }
}