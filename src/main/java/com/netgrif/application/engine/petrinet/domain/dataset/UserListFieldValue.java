package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class UserListFieldValue {

    private List<UserFieldValue> userValues;

    public UserListFieldValue() {
        this(new LinkedList<>());
    }

    public UserListFieldValue(List<UserFieldValue> userValues) {
        this.userValues = userValues;
    }

    public UserListFieldValue(UserFieldValue userValue) {
        this(List.of(userValue));
    }

    @Override
    public String toString() {
        if (userValues == null) {
            return "";
        }
        return userValues.toString();
    }
}
