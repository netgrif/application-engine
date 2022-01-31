package com.netgrif.application.engine.auth.domain;

import java.time.LocalDateTime;

public interface RegisteredUser extends IUser {

    String getToken();

    String getPassword();

    void setPassword(String password);

    void setToken(String token);

    void setExpirationDate(LocalDateTime expirationDate);
}
