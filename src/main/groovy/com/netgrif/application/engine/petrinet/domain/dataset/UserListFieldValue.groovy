package com.netgrif.application.engine.petrinet.domain.dataset

class UserListFieldValue {

    private Set<UserFieldValue> userValues

    UserListFieldValue() {
        this.userValues = new LinkedHashSet<>()
    }

    UserListFieldValue(LinkedHashSet<UserFieldValue> userValues) {
        this()
        this.userValues = userValues
    }

    UserListFieldValue(Set<UserFieldValue> userValues) {
        this()
        this.userValues = new LinkedHashSet<>(userValues)
    }

    UserListFieldValue(List<UserFieldValue> userValues) {
        this()
        this.userValues = new LinkedHashSet<>(userValues)
    }

    LinkedHashSet<UserFieldValue> getUserValues() {
        return userValues
    }

    void setUserValues(Set<UserFieldValue> userValues) {
        this.userValues = new LinkedHashSet<>(userValues)
    }

    void setUserValues(List<UserFieldValue> userValues) {
        this.userValues = new LinkedHashSet<>(userValues)
    }

    @Override
    String toString() {
        return userValues.toString()
    }
}
