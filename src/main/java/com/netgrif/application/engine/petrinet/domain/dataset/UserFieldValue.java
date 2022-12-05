package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Getter;
import lombok.NoArgsConstructor;

// TODO: remove, store only String id
@Getter
public class UserFieldValue {

    protected final String id;
    protected final String name;
    protected final String surname;
    protected final String email;

    // TODO: NAE-1645 no args constructur, constructor from IUser

    public UserFieldValue(String id, String name, String surname, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public String getFullName() {
        return name + " " + surname;
    }
}