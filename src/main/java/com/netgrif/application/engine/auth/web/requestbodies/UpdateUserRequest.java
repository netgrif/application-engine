package com.netgrif.application.engine.auth.web.requestbodies;

import com.netgrif.core.auth.domain.*;
import com.netgrif.core.auth.domain.enums.UserState;
import com.netgrif.core.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Setter
@Getter
public class UpdateUserRequest implements IUser, Serializable {

    @Serial
    private static final long serialVersionUID = 3681503301565489613L;

    public String telNumber;

    public String avatar;

    public String name;

    public String surname;

    public UpdateUserRequest() {
    }

    @Override
    public String getEmail() {
        return "";
    }

    @Override
    public void setEmail(String s) {

    }

    @Override
    public String getRealmId() {
        return "";
    }

    @Override
    public void setRealmId(String s) {

    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public void setUsername(String s) {

    }

    @Override
    public String getFirstName() {
        return "";
    }

    @Override
    public void setFirstName(String s) {

    }

    @Override
    public String getLastName() {
        return "";
    }

    @Override
    public void setLastName(String s) {

    }

    @Override
    public String getFullName() {
        return "";
    }

    @Override
    public UserState getState() {
        return null;
    }

    @Override
    public void setState(UserState userState) {

    }

    @Override
    public LoggedUser transformToLoggedUser() {
        return null;
    }

    @Override
    public Author transformToAuthor() {
        return null;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isImpersonating() {
        return false;
    }

    @Override
    public IUser getSelfOrImpersonated() {
        return null;
    }

    @Override
    public IUser getImpersonated() {
        return null;
    }

    @Override
    public void setImpersonated(IUser iUser) {

    }

    @Override
    public void enableMFA(String s, String s1, int i) {

    }

    @Override
    public void disableMFA(String s) {

    }

    @Override
    public Set<String> getEnabledMFAMethods() {
        return Set.of();
    }

    @Override
    public boolean isMFAEnabled(String s) {
        return false;
    }

    @Override
    public void activateMFA(String s, String s1) {

    }

    @Override
    public void activateMFA(String s, String s1, boolean b) {

    }

    @Override
    public boolean validateRequiredAttributes() {
        return false;
    }

    @Override
    public Credential getCredential(String s) {
        return null;
    }

    @Override
    public <T> Object getCredentialValue(String s) {
        return null;
    }

    @Override
    public void setCredential(String s, String s1, int i, boolean b) {

    }

    @Override
    public void addCredential(Credential<?> credential) {

    }

    @Override
    public void setCredentialProperty(String s, String s1, Object o) {

    }

    @Override
    public Object getCredentialProperty(String s, String s1) {
        return null;
    }

    @Override
    public void removeCredential(String s) {

    }

    @Override
    public boolean hasCredential(String s) {
        return false;
    }

    @Override
    public String getStringId() {
        return "";
    }

    @Override
    public Set<Authority> getAuthorities() {
        return Set.of();
    }

    @Override
    public void setAuthorities(Set<Authority> set) {

    }

    @Override
    public Set<ProcessRole> getProcessRoles() {
        return Set.of();
    }

    @Override
    public void setProcessRoles(Set<ProcessRole> set) {

    }

    @Override
    public Set<String> getGroupIds() {
        return Set.of();
    }

    @Override
    public Set<Group> getGroups() {
        return Set.of();
    }

    @Override
    public void setGroupIds(Set<String> set) {

    }

    @Override
    public void addGroupId(String s) {

    }

    @Override
    public void removeGroupId(String s) {

    }

    @Override
    public void addAuthority(Authority authority) {

    }

    @Override
    public void removeAuthority(Authority authority) {

    }

    @Override
    public void addProcessRole(ProcessRole processRole) {

    }

    @Override
    public void removeProcessRole(ProcessRole processRole) {

    }

    @Override
    public void setAttribute(String s, Object o, boolean b) {

    }

    @Override
    public Object getAttributeValue(String s) {
        return null;
    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public boolean isAttributeSet(String s) {
        return false;
    }

    @Override
    public Attribute<?> getAttribute(String s) {
        return null;
    }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "telNumber='" + telNumber + '\'' +
                ", avatar='" + avatar + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
