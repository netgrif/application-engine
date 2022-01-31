package com.netgrif.application.engine.auth.web.requestbodies;

public class UpdateUserRequest {

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
