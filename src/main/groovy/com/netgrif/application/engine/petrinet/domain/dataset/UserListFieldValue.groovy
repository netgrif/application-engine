package com.netgrif.application.engine.petrinet.domain.dataset;

class UserListFieldValue {

    private List<UserFieldValue> userValues;

    UserListFieldValue(List<UserFieldValue> userValues) {
        this.userValues = userValues;
    }

    List<UserFieldValue> getUserValues() {
        return userValues;
    }

    void setUserValues(List<UserFieldValue> userValues) {
        this.userValues = userValues;
    }

    @Override
    String toString() {
        return userValues.toString();
    }
}
