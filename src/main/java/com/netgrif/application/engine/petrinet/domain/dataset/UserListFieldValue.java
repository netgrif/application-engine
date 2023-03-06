package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Data;

import java.util.*;

@Data
public class UserListFieldValue {

    private LinkedHashSet<UserFieldValue> userValues;

    public UserListFieldValue() {
        this.userValues = new LinkedHashSet<>();
    }

    public UserListFieldValue(Collection<UserFieldValue> userValues) {
        this();
        this.userValues = new LinkedHashSet<>(userValues);
    }

    public UserListFieldValue(UserFieldValue userValue) {
        this(Set.of(userValue));
    }

    public LinkedHashSet<UserFieldValue> getUserValues() {
        return userValues;
    }

    public void setUserValues(Collection<UserFieldValue> userValues) {
        this.userValues = new LinkedHashSet<>(userValues);
    }

    @Override
    public String toString() {
        if (userValues == null) {
            return "";
        }
        return userValues.toString();
    }
}
