package com.netgrif.workflow.petrinet.domain.dataset

class UserFieldValue {

    protected String id

    protected String name

    protected String surname

    protected String email

    UserFieldValue(String id, String name, String surname, String email) {
        this.id = id
        this.name = name
        this.surname = surname
        this.email = email
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