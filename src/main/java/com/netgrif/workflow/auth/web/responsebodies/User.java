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

    protected User(com.netgrif.workflow.auth.domain.User user) {
        id = user.getId();
        email = user.getEmail();
        avatar = user.getAvatar();
        name = user.getName();
        surname = user.getSurname();
        fullName = user.getFullName();
    }

    /**
     * This static method doesn't set attributes regarding the ProcessRoles
     *
     * Use the IUserFactory service to create instances that have these attributes set.
     */
    public static User createSmallUser(com.netgrif.workflow.auth.domain.User user) {
        return new User(user);
    }

    /**
     * This static method doesn't set attributes regarding the ProcessRoles
     *
     * Use the IUserFactory service to create instances that have these attributes set.
     */
    public static User createUser(com.netgrif.workflow.auth.domain.User user) {
        User result = new User(user);
        result.setTelNumber(user.getTelNumber());
        result.setGroups(user.getGroups());
        result.setAuthorities(user.getAuthorities());
        result.setNextGroups(user.getNextGroups());
        return result;
    }
}