package com.netgrif.application.engine.auth.web.requestbodies;

import java.io.Serial;
import java.io.Serializable;

public class UpdateUserRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3681503301565489613L;
    public String telNumber;
    public String avatar;
    public String name;
    public String surname;

    public UpdateUserRequest() {
    }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "telNumber='" + telNumber + '\'' +
                ", avatar='" + avatar + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
