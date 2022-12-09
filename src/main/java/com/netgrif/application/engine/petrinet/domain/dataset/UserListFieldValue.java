package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Data;

import java.util.List;

@Data
public class UserListFieldValue {

    private List<UserFieldValue> userValues;

    public UserListFieldValue(List<UserFieldValue> userValues) {
        this.userValues = userValues;
    }

    @Override
    public String toString() {
        if (userValues == null) {
            return "";
        }
        return userValues.toString();
    }
}
