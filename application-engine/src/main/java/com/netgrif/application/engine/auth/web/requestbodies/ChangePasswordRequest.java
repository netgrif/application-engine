package com.netgrif.application.engine.auth.web.requestbodies;

public class ChangePasswordRequest {

    public String login;

    public String password;

    public String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String login, String password, String newPassword) {
        this.login = login;
        this.password = password;
        this.newPassword = newPassword;
    }
}
