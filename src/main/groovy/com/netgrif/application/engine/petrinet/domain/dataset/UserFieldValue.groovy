package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.auth.domain.IUser

class UserFieldValue {

    protected String id

    protected String name

    protected String surname

    protected String email

    UserFieldValue() {}

    UserFieldValue(String id, String name, String surname, String email) {
        this.id = id
        this.name = name
        this.surname = surname
        this.email = email
    }

    UserFieldValue(IUser user) {
        this.id = user.stringId
        this.name = user.name
        this.surname = user.surname
        this.email = user.email
    }

    String getId() {
        return id
    }

    String getName() {
        return name
    }

    String getSurname() {
        return surname
    }

    String getEmail() {
        return email
    }

    String getFullName() {
        return name + " " + surname
    }
}