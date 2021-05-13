package com.netgrif.workflow.auth.domain;

import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;

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

    Set<Group> getGroups();

    Set<String> getNextGroups();

    void setEmail(String email);

    void setName(String name);

    void setSurname(String surname);

    void setState(UserState state);

    void setProcessRoles(Set<ProcessRole> processRoles);

    void setAuthorities(Set<Authority> authorities);

    void setGroups(Set<Group> groups);

    void setNextGroups(Set<String> nextGroups);

    void addAuthority(Authority authority);

    void addProcessRole(ProcessRole role);

    void removeProcessRole(ProcessRole role);

    LoggedUser transformToLoggedUser();

    boolean isActive();

}
