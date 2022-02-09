package com.netgrif.application.engine.configuration.security.interfaces;


public interface IAuthenticationService {

    void loginSucceeded(String key);

    void loginFailed(String key);

    boolean isIPBlocked(String key);

}