package com.netgrif.application.engine.security.service;

public interface ISecurityContextService {

    void saveToken(String token);

    void clearToken(String token);
}
