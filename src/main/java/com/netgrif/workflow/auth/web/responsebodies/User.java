package com.netgrif.workflow.auth.web.responsebodies;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.orgstructure.domain.Group;
import lombok.Data;

import java.util.Set;

@Data
public class User {

    private Long id;

    private String email;

    private String telNumber;

    private String avatar;

    private String name;

    private String surname;

    private String fullName;

    private Set<Group> groups;

    private Set<Authority> authorities;

    // process roles are set with the factory
    private Set<ProcessRole> processRoles;

    private Set<String> nextGroups;

    /**
     * Equivalent to calling User(user, false)
     */
    public User(com.netgrif.workflow.auth.domain.User user) {
        this(user, false);
    }

    public User(com.netgrif.workflow.auth.domain.User user, boolean small) {
        id = user.getId();
        email = user.getEmail();
        avatar = user.getAvatar();
        name = user.getName();
        surname = user.getSurname();
        fullName = user.getFullName();
        if (!small) {
            telNumber = user.getTelNumber();
            groups = user.getGroups();
            authorities = user.getAuthorities();
            nextGroups = user.getNextGroups();
        }
    }
}