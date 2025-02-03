package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.core.auth.domain.Authority;
import com.netgrif.core.auth.domain.Group;
import com.netgrif.core.auth.domain.IUser;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class User {

    private String id;

    private String email;

    private String telNumber;

    private String avatar;

    private String name;

    private String surname;

    private String fullName;

    private Set<Authority> authorities;

    // process roles are set with the factory
    private Set<ProcessRole> processRoles;

    private Set<String> nextGroups;

    private User impersonated;

    protected User(IUser user) {
        id = user.getStringId();
        email = user.getEmail();
        avatar = user.getAvatar();
        name = user.getFirstName();
        surname = user.getLastName();
        fullName = user.getFullName();
    }

    /**
     * This static method doesn't set attributes regarding the ProcessRoles
     *
     * Use the IUserFactory service to create instances that have these attributes set.
     */
    public static User createSmallUser(IUser user) {
        return new User(user);
    }

    /**
     * This static method doesn't set attributes regarding the ProcessRoles
     *
     * Use the IUserFactory service to create instances that have these attributes set.
     */
    public static User createUser(IUser user) {
        User result = new User(user);
        //MODULARISATION: tel number does not exists on new user
//        result.setTelNumber(user.getTelNumber());
        result.setAuthorities(user.getAuthorities());
        result.setNextGroups(user.getGroups().stream().map(Group::getStringId).collect(Collectors.toSet()));
        return result;
    }
}