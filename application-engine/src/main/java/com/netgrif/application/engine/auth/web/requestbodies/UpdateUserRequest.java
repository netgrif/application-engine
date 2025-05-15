package com.netgrif.application.engine.auth.web.requestbodies;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class UpdateUserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3681503301565489613L;

    public String avatar;

    public String firstName;

    public String middleName;

    public String lastName;

    public String email;

    public UpdateUserRequest() {
    }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "avatar='" + avatar + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
