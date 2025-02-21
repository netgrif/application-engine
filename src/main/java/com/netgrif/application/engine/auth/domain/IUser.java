package com.netgrif.application.engine.auth.domain;

import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import com.netgrif.application.engine.authorization.domain.ProcessRole;

import java.util.Set;

public interface IUser {

    String getStringId();

    String getEmail();

    void setEmail(String email);

    String getName();

    void setName(String name);

    String getSurname();

    void setSurname(String surname);

    String getFullName();

    String getAvatar();

    String getTelNumber();

    UserState getState();

    void setState(UserState state);

    Set<Authority> getAuthorities();

    void setAuthorities(Set<Authority> authorities);

    Set<RoleAssignment> getRoleAssignments();

    void setRoleAssignments(Set<RoleAssignment> roleAssignments);

    Set<String> getNextGroups();

    void setNextGroups(Set<String> nextGroups);

    void addGroup(String groupId);

    void removeGroup(String groupId);

    void addAuthority(Authority authority);

    void addRole(ProcessRole processRole);

    void removeRole(ProcessRole processRole);

    LoggedUser transformToLoggedUser();

    Author transformToAuthor();

    boolean isActive();

    boolean isImpersonating();

    IUser getSelfOrImpersonated();

    IUser getImpersonated();

    void setImpersonated(IUser user);

}
