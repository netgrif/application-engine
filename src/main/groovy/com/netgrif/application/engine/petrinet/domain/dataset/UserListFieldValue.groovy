package com.netgrif.application.engine.petrinet.domain.dataset

class UserListFieldValue {

    private Set<UserFieldValue> userValues

    UserListFieldValue() {
        this.userValues = new LinkedHashSet<>()
    }

    UserListFieldValue(Collection<UserFieldValue> userValues) {
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
