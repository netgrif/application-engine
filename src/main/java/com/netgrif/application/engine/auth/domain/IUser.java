package com.netgrif.application.engine.auth.domain;

import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;

import java.util.Set;

public interface IUser {

    String getStringId();

    String getEmail();

    String getName();

    String getSurname();

    String getFullName();

    String getAvatar();

    String getTelNumber();

    UserState getState();

    Set<Authority> getAuthorities();

    Set<ProcessRole> getProcessRoles();

    Set<String> getNextGroups();

    void setEmail(String email);

    void setName(String name);

    void setSurname(String surname);

    void setState(UserState state);

    void setProcessRoles(Set<ProcessRole> processRoles);

    void setAuthorities(Set<Authority> authorities);

    void setNextGroups(Set<String> nextGroups);

    void addGroup(String groupId);

    void removeGroup(String groupId);

    void addAuthority(Authority authority);

    void addProcessRole(ProcessRole role);

    void removeProcessRole(ProcessRole role);

    LoggedUser transformToLoggedUser();

    Author transformToAuthor();

    boolean isActive();

    void setImpersonated(IUser user);

    boolean isImpersonating();

    IUser getSelfOrImpersonated();

    IUser getImpersonated();

}
