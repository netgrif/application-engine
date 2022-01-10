package com.netgrif.workflow.ldap.service.interfaces;


public interface IAuthenticationService {

    void loginSucceeded(String key);

    void loginFailed(String key);

    boolean isIPBlocked(String key);

}