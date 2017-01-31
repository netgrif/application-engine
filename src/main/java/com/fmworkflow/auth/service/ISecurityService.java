package com.fmworkflow.auth.service;

public interface ISecurityService {
    String findLoggedInUsername();
    void autologin(String username, String password);
}
