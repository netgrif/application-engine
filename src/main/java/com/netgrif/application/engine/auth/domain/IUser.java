//package com.netgrif.core.auth.domain.Authority;;
//
//import com.netgrif.core.petrinet.domain.roles.ProcessRole;
//
//import java.util.Set;
//
//public interface IUser {
//
//    String getStringId();
//
//    String getEmail();
//
//    void setEmail(String email);
//
//    String getName();
//
//    void setName(String name);
//
//    String getSurname();
//
//    void setSurname(String surname);
//
//    String getFullName();
//
//    String getAvatar();
//
//    String getTelNumber();
//
//    UserState getState();
//
//    void setState(UserState state);
//
//    Set<Authority> getAuthorities();
//
//    void setAuthorities(Set<Authority> authorities);
//
//    Set<ProcessRole> getProcessRoles();
//
//    void setProcessRoles(Set<ProcessRole> processRoles);
//
//    Set<String> getNextGroups();
//
//    void setNextGroups(Set<String> nextGroups);
//
//    void addGroup(String groupId);
//
//    void removeGroup(String groupId);
//
//    void addAuthority(Authority authority);
//
//    void addProcessRole(ProcessRole role);
//
//    void removeProcessRole(ProcessRole role);
//
//    LoggedUser transformToLoggedUser();
//
//    Author transformToAuthor();
//
//    boolean isActive();
//
//    boolean isImpersonating();
//
//    IUser getSelfOrImpersonated();
//
//    IUser getImpersonated();
//
//    void setImpersonated(IUser user);
//
//}
