package com.netgrif.application.engine.petrinet.domain.dataset;

class UserListFieldValue {

    private LinkedHashSet<UserFieldValue> userValues

    UserListFieldValue(LinkedHashSet<UserFieldValue> userValues) {
        this.userValues = userValues
    }

    LinkedHashSet<UserFieldValue> getUserValues() {
        return userValues
    }

    void setUserValues(LinkedHashSet<UserFieldValue> userValues) {
        this.userValues = userValues
    }

    @Override
    String toString() {
        return userValues.toString()
    }
}
