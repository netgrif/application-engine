package com.netgrif.workflow.auth.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.Organization;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserProcessRole;
import lombok.Data;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@JsonRootName("user")
public class LocalisedUser {

    private Long id;

    private String email;

    private String password;

    private String telNumber;

    private String avatar;

    private String name;

    private String surname;

    private String fullName;

    private Set<Organization> organizations;

    private Set<Authority> authorities;

    private Set<LocalisedProcessRole> processRoles;

    private Set<UserProcessRole> userProcessRoles;

    public LocalisedUser(User user, Locale locale) {
        id = user.getId();
        email = user.getEmail();
        password = user.getPassword();
        telNumber = user.getTelNumber();
        avatar = user.getAvatar();
        name = user.getName();
        surname = user.getSurname();
        fullName = user.getFullName();
        organizations = user.getOrganizations();
        authorities = user.getAuthorities();
        userProcessRoles = user.getUserProcessRoles();
        processRoles = user.getProcessRoles().stream()
                .map(role -> new LocalisedProcessRole(role, locale))
                .collect(Collectors.toSet());
    }
}