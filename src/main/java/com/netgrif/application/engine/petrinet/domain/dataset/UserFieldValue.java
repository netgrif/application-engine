package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.auth.domain.IUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

// TODO: NAE-1645 remove, store only ObjectId
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFieldValue {

    private String id;
    private String name;
    private String surname;
    private String email;

    public UserFieldValue(IUser user) {
        this.id = user.getStringId();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.email = user.getEmail();
    }

    public String getFullName() {
        return name + " " + surname;
    }
}