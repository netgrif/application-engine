package com.fmworkflow.auth.service.interfaces;

public interface ISecurityService {
    String findLoggedInUsername();
    void autologin(String username, String password);
}
