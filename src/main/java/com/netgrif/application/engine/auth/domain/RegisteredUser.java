package com.netgrif.application.engine.auth.domain;

import java.time.LocalDateTime;

public interface RegisteredUser extends IUser {

    String getToken();

    void setToken(String token);

    String getPassword();

    void setPassword(String password);

    void setExpirationDate(LocalDateTime expirationDate);
}
