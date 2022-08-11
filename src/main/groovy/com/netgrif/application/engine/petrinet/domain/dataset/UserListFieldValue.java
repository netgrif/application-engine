package com.netgrif.application.engine.petrinet.domain.dataset;

import org.elasticsearch.action.search.SearchTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserListFieldValue {

    private List<UserFieldValue> userValues;

    public UserListFieldValue(List<UserFieldValue> userValues) {
        this.userValues = userValues;
    }

    public List<UserFieldValue> getUserValues() {
        return userValues;
    }

    public void setUserValues(List<UserFieldValue> userValues) {
        this.userValues = userValues;
    }

    @Override
    public String toString() {
        return userValues.toString();
    }
}
