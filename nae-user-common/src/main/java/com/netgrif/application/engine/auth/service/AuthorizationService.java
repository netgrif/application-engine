package com.netgrif.application.engine.auth.service;

public interface AuthorizationService {
    boolean hasAuthority(String authority);
}
