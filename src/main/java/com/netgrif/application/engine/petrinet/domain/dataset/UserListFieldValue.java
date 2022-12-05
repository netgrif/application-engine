package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Data;

import java.util.List;

@Data
public class UserListFieldValue {

    private List<UserFieldValue> userValues;

    public UserListFieldValue(List<UserFieldValue> userValues) {
        this.userValues = userValues;
    }

    // TODO: NAE-1645 6.2.5 toString
}
